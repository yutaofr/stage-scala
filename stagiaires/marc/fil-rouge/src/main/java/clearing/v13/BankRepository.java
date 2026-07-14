package clearing.v13;

import clearing.model.Bank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public final class BankRepository {
  private final ArrayList<Bank> banks;

  public BankRepository() {
    this(
        List.of(
            new Bank("ATH", "Attijariwafa Bank"),
            new Bank("CIH", "CIH Bank"),
            new Bank("BOA", "Bank of Africa"),
            new Bank("BMCE", "BMCE Capital"),
            new Bank("SGMB", "Société Générale Maroc")));
  }

  public BankRepository(List<Bank> banks) {
    this.banks = new ArrayList<>(banks);
  }

  public ArrayList<Bank> getAllBanks() {
    return new ArrayList<>(banks);
  }
}
