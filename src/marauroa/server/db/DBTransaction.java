package marauroa.server.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.adapter.DatabaseAdapter;
import marauroa.server.game.db.StringChecker;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * a database transaction
 *
 * @author hendrik
 */
public class DBTransaction {
    private static Logger logger = Log4J.getLogger(DBTransaction.class);

	private DatabaseAdapter databaseAdapter = null;
	private LinkedList<Statement> statements = null;
	private LinkedList<ResultSet> resultSets = null;
    private RE reInt;
    private RE reIntList;

	/**
	 * creates a new DBTransaction
	 *
	 * @param databaseAdapter database adapter for accessing the database
	 */
	protected DBTransaction(DatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
        try {
            reInt = new RE("^[0-9 ]*$");
            reIntList = new RE("^[0-9, ]*$");
        } catch (RESyntaxException e) {
            logger.error(e, e);
        }
	}

	/**
	 * trys to commits this transaction, in case the commit fails, a rollback is executed.
	 *
	 * @throws SQLException in case of an database error
	 */
	protected void commit() throws SQLException {
		try {
			databaseAdapter.commit();
		} catch (SQLException e) {
			databaseAdapter.rollback();
			throw e;
		}
	}

	/**
	 * rollsback this transaction
	 */
	protected void rollback() {
		try {
			databaseAdapter.rollback();
		} catch (SQLException e) {
			logger.error(e, e);
		}
	}


    /**
     * Replaces variables SQL-Statements and prevents SQL injection attacks
     *
     * @param sql SQL-String
     * @param params replacement parameters
     * @return SQL-String with substitued parameters
     * @throws SQLException in case of an sql injection attack
     */
    public String subst(String sql, Map<String, ?> params) throws SQLException {
        StringBuffer res = new StringBuffer();
        StringTokenizer st = new StringTokenizer(sql, "([]'", true);
        String lastToken = "";
        String secondLastToken = "";
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (lastToken.equals("[")) {

                // Variablen ersetzen
                Object temp = params.get(token);
                if (temp != null) {
                    token = temp.toString();
                } else {
                    token = "";
                }

                // SQL-Injection abfangen
                if (secondLastToken.equals("(")) {
                    if (!reIntList.match(token)) {
                        throw new SQLException("Illegal argument: \"" + token + "\" is not an integer list"); 
                    }
                } else if (secondLastToken.equals("'")) {
                    if (token.length() > 0) {
                        token = StringChecker.escapeSQLString(token);
                    }
                } else {
                    if (!reInt.match(token)) {
                        throw new SQLException("Illegal argument: \"" + token + "\" is not an integer."); 
                    }
                }
            }
            secondLastToken = lastToken;
            lastToken = token.trim();
            if (token.equals("[") || token.equals("]")) {
                token = "";
            }
            res.append(token);
        }
        return res.toString();
    }	
}