package dto;

import java.util.ArrayList;
import java.util.List;

public record CollectionPoint(int id, String adresse, Integer capaciteMax, List<WasteType> wasteTypes) {
    public CollectionPoint() {
        this(0, "N/A", 0, new ArrayList<>());
    }

    public CollectionPoint(int id, String adresse, Integer capaciteMax, List<WasteType> wasteTypes) {
        this.id = id;
        this.adresse = adresse;
        this.capaciteMax = capaciteMax;
        this.wasteTypes = wasteTypes;
    }

    
    
}
