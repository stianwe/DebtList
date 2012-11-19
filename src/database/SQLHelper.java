package database;

import java.sql.SQLException;
import java.sql.Statement;

import logic.Debt;

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
	
	/**
	 * Executes a SQL insert with the given values
	 * @param st			The statement to execute the insert on
	 * @param tableName		The name of the target table
	 * @param fieldNames	The field names that shall be inserted
	 * @param fieldValues	The field values that shall be inserted
	 * @throws SQLException
	 */
	public static void insert(Statement st, String tableName, String[] fieldNames, String[] fieldValues) throws SQLException {
		if(fieldNames.length != fieldValues.length)
			throw new IndexOutOfBoundsException("fieldNames and fieldValues must be of the same length.");
		StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
		StringBuilder values = new StringBuilder();
		for (int i = 0; i < fieldValues.length; i++) {
			query.append(fieldNames[i] + (i < fieldValues.length - 1 ? ", " : ") VALUES ("));
			values.append(fieldValues[i] + (i < fieldValues.length - 1 ? ", " : ")"));
		}
		query.append(values);
		st.executeUpdate(query.toString());
	}
	
	/**
	 * Will check if the debt given as argument exists in the database, and will
	 * 	o update the debt if it already exists (as of now the only updateable field is the status)
	 * 	o insert the debt if it is not present in the database
	 * @param st	The statement to execute the update/insertion on
	 * @param d		The debt to update/insert
	 * @return		True if the debt was updated, false if it was inserted (created)
	 * @throws SQLException 
	 */
	public static boolean updateDebt(Statement st, Debt d) throws SQLException {
		// Check if the debt already exists
		if(SQLHelper.exists(st, DatabaseUnit.TABLE_DEBT, DatabaseUnit.FIELD_DEBT_ID, d.getId() + "")) {
			// Update the values that can change
			// TODO: Anything else that can change than the status??
			SQLHelper.update(st, DatabaseUnit.TABLE_DEBT, new String[]{DatabaseUnit.FIELD_DEBT_STATUS}, new String[]{d.getStatus() + ""}, DatabaseUnit.FIELD_DEBT_ID, d.getId() + "");
			return true;
		} else {
			// If not, create it
			SQLHelper.insert(st, DatabaseUnit.TABLE_DEBT, new String[]{DatabaseUnit.FIELD_DEBT_ID, DatabaseUnit.FIELD_DEBT_AMOUNT, DatabaseUnit.FIELD_DEBT_WHAT, DatabaseUnit.FIELD_DEBT_TO_USER, DatabaseUnit.FIELD_DEBT_FROM_USER, DatabaseUnit.FIELD_DEBT_REQUESTED_BY_USER, DatabaseUnit.FIELD_DEBT_COMMENT, DatabaseUnit.FIELD_DEBT_STATUS}, 
					new String[]{d.getId()+"", d.getAmount()+"", d.getWhat(), d.getTo().getUsername(), d.getFrom().getUsername(), d.getRequestedBy().getUsername(), d.getComment(), d.getStatus().toString()});
			return false;
		}
	}
}
