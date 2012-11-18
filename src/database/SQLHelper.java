package database;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * A simple helper class to generate (and execute) SQL queries
 */
public class SQLHelper {

	/**
	 * Generates a SQL query to check if a specified row exists in the given table, using a query on the form:
	 * SELECT EXISTS(SELECT 1 FROM <tableName> WHERE <fieldNameToCheck>=<fieldValueToCheck>
	 * @param tableName			The table name
	 * @param fieldNameToCheck	The field (or column) to check
	 * @param fieldValueToCheck	The field (or column) value to check for
	 * @return					The generated SQL query
	 */
	public static String existsQuery(String tableName, String fieldNameToCheck, String fieldValueToCheck) {
		return "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE " + fieldNameToCheck + "=" + fieldValueToCheck + ")";
	}
	
	/**
	 * Executes a SQL query to check if a specified row exists in the given table, using a query on the form:
	 * SELECT EXISTS(SELECT 1 FROM <tableName> WHERE <fieldNameToCheck>=<fieldValueToCheck>
	 * @param st				The SQL statement to use
	 * @param tableName			The table name
	 * @param fieldNameToCheck	The field (or column) to check
	 * @param fieldValueToCheck	The field (or column) value to check for
	 * @throws SQLException
	 * @return					True if the row exists, false if not
	 */
	public static boolean exists(Statement st, String tableName, String fieldNameToCheck, String fieldValueToCheck) throws SQLException {
		return st.executeQuery(existsQuery(tableName, fieldNameToCheck, fieldValueToCheck)).next();
	}
	
	/**
	 * Executes a SQL update with the given values
	 * @param st					The statement to do the update on
	 * @param tableName				The table to update
	 * @param fieldNamesToUpdate	The fields (or columns) to update
	 * @param fieldValuesToUpdate	The fields' (or columns') values to update to
	 * @param rowIdFieldName		The field (or column) to use to id the correct row, null for all rows
	 * @param rowIdFieldValue		The field's (or column's) value to check to id the correct row, null for all rows
	 * @throws SQLException 
	 */
	public static void update(Statement st, String tableName, String[] fieldNamesToUpdate, String[] fieldValuesToUpdate, String rowIdFieldName, String rowIdFieldValue) throws SQLException {
		if(fieldNamesToUpdate.length != fieldValuesToUpdate.length)
			throw new IndexOutOfBoundsException("The lengths of the fieldNamesToUpdate and fieldValuesToUpdate must be equal.");
		StringBuilder sb = new StringBuilder("UPDATE " + tableName + " SET ");
		for (int i = 0; i < fieldValuesToUpdate.length; i++) {
			sb.append(fieldNamesToUpdate[i] + "=" + fieldValuesToUpdate[i] + (i < fieldValuesToUpdate.length - 1 ? ", " : ""));
		}
		// Add WHERE clause if specified
		if(rowIdFieldName != null && rowIdFieldValue != null) {
			sb.append(" WHERE " + rowIdFieldName + "=" + rowIdFieldValue);
		}
		st.executeUpdate(sb.toString());
	}
}
