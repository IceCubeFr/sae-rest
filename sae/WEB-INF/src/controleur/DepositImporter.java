package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.Arrays;

import dao.DepositDAO;
import dto.Deposit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/import")
@MultipartConfig
public class DepositImporter extends HttpServlet{
    private static final String SEPARATEUR_CSV = ";";
    private static final String[] COLONNES_ATTENDUES = new String[]{"id","userid","pointid","poids","wastetypeid","datedepot"};

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            Part filePart = req.getPart("csvFile");
            if (filePart == null) {
                out.println("Erreur: Aucun fichier n'a été uploadé.");
                return;
            }
            
            try (InputStream fileContent = filePart.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent))) {
                String line;
                DepositDAO ddao = new DepositDAO();
                int rowCount = 0;
                line = reader.readLine(); 
                
                if (line == null) {
                    out.println("Erreur: Le fichier est vide.");
                    return;
                }
                
                String[] colonnesTrouvees = line.split(SEPARATEUR_CSV);

                if (colonnesTrouvees.length != COLONNES_ATTENDUES.length) {
                    out.println(
                        "Erreur d'en-tête : Le nombre de colonnes trouvées (" + colonnesTrouvees.length + 
                        ") ne correspond pas au nombre attendu (" + COLONNES_ATTENDUES.length + ")."
                    );
                    return;
                }
                for (int i = 0; i < COLONNES_ATTENDUES.length; i++) {
                    if (!colonnesTrouvees[i].trim().equalsIgnoreCase(COLONNES_ATTENDUES[i].trim())) {
                        out.println(
                            "Erreur d'en-tête : La colonne '" + colonnesTrouvees[i] + 
                            "' est différente de la colonne attendue '" + COLONNES_ATTENDUES[i] + "' (position " + (i+1) + ")."
                        );
                        return;
                    }
                }
                
                out.println("En-têtes CSV vérifiés avec succès : " + Arrays.toString(COLONNES_ATTENDUES) + "<br/>");
                
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] values = line.split(SEPARATEUR_CSV);
                        if(ddao.add(new Deposit(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[4]), Integer.parseInt(values[3]), Date.valueOf(values[4]), Boolean.parseBoolean(values[5])))) {
                            rowCount++;
                            out.println("Ligne ajoutée : " + line);
                        } else {
                            out.println("Import non effectué de la ligne : " + line);
                        }
                    } catch (NumberFormatException ex) {
                        out.println("Valeurs invalides : " + line);
                    }
                    
                }
                out.println("Fichier CSV traité avec succès. Nombre de lignes de données traitées: " + rowCount);
                
            } catch (Exception e) {
                out.println("Une erreur est survenue lors du traitement du fichier: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            out.println("Erreur lors de l'upload du fichier: " + e.getMessage());
            e.printStackTrace();
        }
    }
}