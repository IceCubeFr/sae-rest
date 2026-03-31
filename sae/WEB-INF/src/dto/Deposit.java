package dto;

import java.sql.Date;

public record Deposit(Integer id, Integer userid, Integer pointid, Integer wastetypeid, Integer poids, Date datedepot, Boolean collecte) {
    
}
