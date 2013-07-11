/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jkiss.dbeaver.ui.editors.sql.format;

import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL formatter
 */
public class SQLFormatter {
    private final SQLParser fParser;

    private SQLFormatterConfiguration formatterCfg = null;
    private List<Boolean> functionBracket = new ArrayList<Boolean>();

    public SQLFormatter(final SQLFormatterConfiguration cfg) {
        formatterCfg = cfg;
        fParser = new SQLParser(cfg);
    }

    public String format(final String argSql)
    {
        functionBracket.clear();

        boolean isSqlEndsWithNewLine = false;
        if (argSql.endsWith("\n")) { //$NON-NLS-1$
            isSqlEndsWithNewLine = true;
        }

        List<SQLFormatterToken> list = fParser.parse(argSql);
        list = format(list);

        StringBuilder after = new StringBuilder(argSql.length() + 20);
        for (SQLFormatterToken token : list) {
            after.append(token.getString());
        }

        if (isSqlEndsWithNewLine) {
            after.append(ContentUtils.getDefaultLineSeparator());
        }

        return after.toString();
    }

    private List<SQLFormatterToken> format(final List<SQLFormatterToken> argList) {
        if (argList.isEmpty()) {
            return argList;
        }

        SQLFormatterToken token = argList.get(0);
        if (token.getType() == SQLFormatterConstants.SPACE) {
            argList.remove(0);
            if (argList.isEmpty()) {
                return argList;
            }
        }

        token = argList.get(argList.size() - 1);
        if (token.getType() == SQLFormatterConstants.SPACE) {
            argList.remove(argList.size() - 1);
            if (argList.isEmpty()) {
                return argList;
            }
        }

        for (int index = 0; index < argList.size(); index++) {
            token = argList.get(index);
            if (token.getType() == SQLFormatterConstants.KEYWORD) {
                switch (formatterCfg.getKeywordCase()) {
                case SQLFormatterConfiguration.KEYWORD_NONE:
                    break;
                case SQLFormatterConfiguration.KEYWORD_UPPER_CASE:
                    token.setString(token.getString().toUpperCase());
                    break;
                case SQLFormatterConfiguration.KEYWORD_LOWER_CASE:
                    token.setString(token.getString().toLowerCase());
                    break;
                }
            }
        }

        for (int index = argList.size() - 1; index >= 1; index--) {
            token = argList.get(index);
            SQLFormatterToken prevToken = argList.get(index - 1);
            if (token.getType() == SQLFormatterConstants.SPACE && (prevToken.getType() == SQLFormatterConstants.SYMBOL || prevToken.getType() == SQLFormatterConstants.COMMENT)) {
                argList.remove(index);
            } else if ((token.getType() == SQLFormatterConstants.SYMBOL || token.getType() == SQLFormatterConstants.COMMENT) && prevToken.getType() == SQLFormatterConstants.SPACE) {
                argList.remove(index - 1);
            } else if (token.getType() == SQLFormatterConstants.SPACE) {
                token.setString(" "); //$NON-NLS-1$
            }
        }

        for (int index = 0; index < argList.size() - 2; index++) {
            SQLFormatterToken t0 = argList.get(index);
            SQLFormatterToken t1 = argList.get(index + 1);
            SQLFormatterToken t2 = argList.get(index + 2);

            if (t0.getType() == SQLFormatterConstants.KEYWORD
                    && t1.getType() == SQLFormatterConstants.SPACE
                    && t2.getType() == SQLFormatterConstants.KEYWORD) {
                if (((t0.getString().equalsIgnoreCase("ORDER") || t0 //$NON-NLS-1$
                        .getString().equalsIgnoreCase("GROUP")) && t2 //$NON-NLS-1$
                        .getString().equalsIgnoreCase("BY"))) { //$NON-NLS-1$
                    t0.setString(t0.getString() + " " + t2.getString()); //$NON-NLS-1$
                    argList.remove(index + 1);
                    argList.remove(index + 1);
                }
            }

            // Oracle style joins
            if (t0.getString().equals("(") && t1.getString().equals("+") && t2.getString().equals(")")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                t0.setString("(+)"); //$NON-NLS-1$
                argList.remove(index + 1);
                argList.remove(index + 1);
            }
        }

        int indent = 0;
        final List<Integer> bracketIndent = new ArrayList<Integer>();
        SQLFormatterToken prev = new SQLFormatterToken(SQLFormatterConstants.SPACE, " "); //$NON-NLS-1$
        boolean encounterBetween = false;
        for (int index = 0; index < argList.size(); index++) {
            token = argList.get(index);
            String tokenString = token.getString().toUpperCase();
            if (token.getType() == SQLFormatterConstants.SYMBOL) {
                if (tokenString.equals("(")) { //$NON-NLS-1$
                    functionBracket.add(formatterCfg.isFunction(prev.getString()) ? Boolean.TRUE : Boolean.FALSE);
                    bracketIndent.add(indent);
                    indent++;
                    index += insertReturnAndIndent(argList, index + 1, indent);
                } else if (tokenString.equals(")")) { //$NON-NLS-1$
                    indent = bracketIndent.remove(bracketIndent.size() - 1);
                    index += insertReturnAndIndent(argList, index, indent);
                    functionBracket.remove(functionBracket.size() - 1);
                } else if (tokenString.equals(",")) { //$NON-NLS-1$
                    index += insertReturnAndIndent(argList, index + 1, indent);
                } else if (tokenString.equals(";")) { //$NON-NLS-1$
                    indent = 0;
                    index += insertReturnAndIndent(argList, index, indent);
                }
            } else if (token.getType() == SQLFormatterConstants.KEYWORD) {
                if (tokenString.equals("DELETE") //$NON-NLS-1$
                        || tokenString.equals("SELECT") //$NON-NLS-1$
                        || tokenString.equals("UPDATE")) //$NON-NLS-1$
                {
                    indent++;
                    index += insertReturnAndIndent(argList, index + 1, indent);
                }
                if (tokenString.equals("INSERT") //$NON-NLS-1$
                        || tokenString.equals("INTO") //$NON-NLS-1$
                        || tokenString.equals("CREATE") //$NON-NLS-1$
                        || tokenString.equals("DROP") //$NON-NLS-1$
                        || tokenString.equals("TRUNCATE") //$NON-NLS-1$
                        || tokenString.equals("TABLE") //$NON-NLS-1$
                        || tokenString.equals("CASE")) { //$NON-NLS-1$
                    indent++;
                    index += insertReturnAndIndent(argList, index + 1, indent);
                }
                if (tokenString.equals("FROM") //$NON-NLS-1$
                        || tokenString.equals("WHERE") //$NON-NLS-1$
                        || tokenString.equals("SET") //$NON-NLS-1$
                        || tokenString.equals("ORDER BY") //$NON-NLS-1$
                        || tokenString.equals("GROUP BY") //$NON-NLS-1$
                        || tokenString.equals("HAVING")) { //$NON-NLS-1$
                    index += insertReturnAndIndent(argList, index, indent - 1);
                    index += insertReturnAndIndent(argList, index + 1, indent);
                }
                if (tokenString.equals("VALUES")) { //$NON-NLS-1$
                    indent--;
                    index += insertReturnAndIndent(argList, index, indent);
                }
                if (tokenString.equals("END")) { //$NON-NLS-1$
                    indent--;
                    index += insertReturnAndIndent(argList, index, indent);
                }
                if (tokenString.equals("OR") //$NON-NLS-1$
                        || tokenString.equals("THEN") //$NON-NLS-1$
                        || tokenString.equals("ELSE")) { //$NON-NLS-1$
                    index += insertReturnAndIndent(argList, index, indent);
                }
                if (tokenString.equals("ON") || tokenString.equals("USING")) { //$NON-NLS-1$ //$NON-NLS-2$
                    index += insertReturnAndIndent(argList, index, indent + 1);
                }
                if (tokenString.equals("UNION") //$NON-NLS-1$
                    || tokenString.equals("INTERSECT") //$NON-NLS-1$
                    || tokenString.equals("EXCEPT")) //$NON-NLS-1$
                {
                    indent -= 2;
                    index += insertReturnAndIndent(argList, index, indent);
                    //index += insertReturnAndIndent(argList, index + 1, indent);
                    indent++;
                }
                if (tokenString.equals("BETWEEN")) { //$NON-NLS-1$
                    encounterBetween = true;
                }
                if (tokenString.equals("AND")) { //$NON-NLS-1$
                    if (!encounterBetween) {
                        index += insertReturnAndIndent(argList, index, indent);
                    }
                    encounterBetween = false;
                }
            } else if (token.getType() == SQLFormatterConstants.COMMENT) {
                Pair<String, String> mlComments = formatterCfg.getSyntaxManager().getKeywordManager().getMultiLineComments();
                if (mlComments != null) {
                    if (token.getString().startsWith(mlComments.getFirst())) {
                        index += insertReturnAndIndent(argList, index + 1, indent);
                    }
                }
            }
            prev = token;
        }

        for (int index = argList.size() - 1; index >= 4; index--) {
            if (index >= argList.size()) {
                continue;
            }

            SQLFormatterToken t0 = argList.get(index);
            SQLFormatterToken t1 = argList.get(index - 1);
            SQLFormatterToken t2 = argList.get(index - 2);
            SQLFormatterToken t3 = argList.get(index - 3);
            SQLFormatterToken t4 = argList.get(index - 4);

            if (t4.getString().equalsIgnoreCase("(") //$NON-NLS-1$
                    && t3.getString().trim().isEmpty()
                    && t1.getString().trim().isEmpty()
                    && t0.getString().equalsIgnoreCase(")")) { //$NON-NLS-1$
                t4.setString(t4.getString() + t2.getString() + t0.getString());
                argList.remove(index);
                argList.remove(index - 1);
                argList.remove(index - 2);
                argList.remove(index - 3);
            }
        }

        for (int index = 1; index < argList.size(); index++) {
            prev = argList.get(index - 1);
            token = argList.get(index);

            if (prev.getType() != SQLFormatterConstants.SPACE &&
                token.getType() != SQLFormatterConstants.SPACE &&
                !token.getString().startsWith("("))
            {
                if (prev.getString().equals(",")) { //$NON-NLS-1$
                    continue;
                }
                if (formatterCfg.isFunction(prev.getString())
                        && token.getString().equals("(")) { //$NON-NLS-1$
                    continue;
                }
                if (token.getType() == SQLFormatterConstants.VALUE && prev.getType() == SQLFormatterConstants.NAME) {
                    // Do not add space between name and value [JDBC:MSSQL]
                    continue;
                }
                argList.add(index, new SQLFormatterToken(SQLFormatterConstants.SPACE, " ")); //$NON-NLS-1$
            }
        }

        return argList;
    }

    private int insertReturnAndIndent(final List<SQLFormatterToken> argList, final int argIndex, final int argIndent)
    {
        if (functionBracket.contains(Boolean.TRUE))
            return 0;
        try {
            String s = ContentUtils.getDefaultLineSeparator();
            final SQLFormatterToken prevToken = argList.get(argIndex - 1);
            if (prevToken.getType() == SQLFormatterConstants.COMMENT && prevToken.getString().startsWith("--")) { //$NON-NLS-1$
                s = ""; //$NON-NLS-1$
            }
            for (int index = 0; index < argIndent; index++) {
                s += formatterCfg.getIndentString();
            }

            SQLFormatterToken token = argList.get(argIndex);
            if (token.getType() == SQLFormatterConstants.SPACE) {
                token.setString(s);
                return 0;
            }

            token = argList.get(argIndex - 1);
            if (token.getType() == SQLFormatterConstants.SPACE) {
                token.setString(s);
                return 0;
            }
            argList.add(argIndex, new SQLFormatterToken(SQLFormatterConstants.SPACE, s));
            return 1;
        } catch (IndexOutOfBoundsException e) {
            // e.printStackTrace();
            return 0;
        }
    }

}