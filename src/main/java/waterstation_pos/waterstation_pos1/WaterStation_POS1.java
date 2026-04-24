/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package waterstation_pos.waterstation_pos1;

/**
 *
 * @author wendevlife
 */
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class WaterStation_POS1 {

    public static void main(String[] args) {
        // Setup FlatLaf Look and Feel
        try {
            javax.swing.UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Run the GUI application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
}
