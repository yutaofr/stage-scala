package clearing.v13;

import java.util.ArrayList;
import java.util.HashMap;

public final class LegacyJavaMock {
    public ArrayList<String> transactionCodes() {
        ArrayList<String> codes = new ArrayList<>();
        codes.add("TX-001");
        codes.add("AUDIT-002");
        codes.add("TX-003");
        codes.add("REJECT-004");
        return codes;
    }

    public HashMap<String, Double> bankBalances() {
        HashMap<String, Double> balances = new HashMap<>();
        balances.put("ATH", 10.25d);
        balances.put("CIH", -3.5d);
        balances.put("BOA", 0.1d);
        return balances;
    }
}
