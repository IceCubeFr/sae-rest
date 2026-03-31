package dto;

public record WasteType(int id, String nom, int pointsPerKilos) {
    public WasteType() {
        this(0, "N/A", 0);
    }

    public WasteType(int id, String nom, int pointsPerKilos) {
        this.id = id;
        this.nom = nom;
        this.pointsPerKilos = pointsPerKilos;
    }
}
