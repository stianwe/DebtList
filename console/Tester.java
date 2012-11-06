package console;

import logic.Debt;
import logic.User;
import session.Session;

public class Tester {
	 public static void main(String[] args) {
         User u = new User("Stian", "asd");
         Session.session.setUser(u);
         User u2 = new User("Arne", "qazqaz");
         User steinastn = new User("Jan", "steinkis");
         User niggah = new User("Jonas", "husetmitt");
         User tolvteplass = new User("Jørgen", "eideideid");
         User hotchick = new User("Jessica Alba", "nam");

         u.addConfirmedDebt(new Debt(1, 2, "lol", u, Session.session.get$
         u.addPendingDebt(new Debt(2, 20, "Boller fra Jan", u2, u, "Haha$
         steinastn.addPendingDebt(new Debt(3,999, "Stein", niggah, stein$
         u2.addConfirmedDebt(new Debt(4,69, "69", hotchick, u2, "Ofc", h$


         // TODO: Add fleir debts her! (B�de pending og confirmed, og fr$
         //              Du kain og pr�v me l�nger brukernavn

         Main.processLs();
	 }
}
