package logic;

public enum DebtStatus {

	REQUESTED, CONFIRMED, DECLINED, COMPLETED_BY_TO, COMPLETED_BY_FROM, COMPLETED;
	
	public static int NUMBER_OF_STATUSES = 6;
	
	/*public String toString() {
		for (int i = 0; i < NUMBER_OF_STATUSES; i++) {
			if(this.ordinal() == i) {
				return i + "";
			}
		}
		return -1 + "";
	}*/
}
