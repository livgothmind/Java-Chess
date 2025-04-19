package src.main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class Main extends JPanel {
    private final int TILE_SIZE = 80; // dimensione ogni casellina
    private final int BOARD_SIZE = 8; // ricordo che scacchiera è una 8x8
    private final int BOARD_PIXEL_SIZE = TILE_SIZE * BOARD_SIZE;

    private boolean showAtariScreen = true;
    private BufferedImage atariLogo; // immagine per il logo inziale
    private BufferedImage[][] pieceImages = new BufferedImage[8][8]; // immagini della scacchiera matriciale
    private final Map<String, BufferedImage> pieceImageMap = new HashMap<>(); // mappatura delle immagini
    private Board logicalBoard; // riferimento alla scacchiera logica
    private int selectedRow = -1, selectedCol = -1; // posizioni del pezzo selezionato

    public Main() {
        loadAtari();
        logicalBoard = new Board(); // SCACCHIERA LOGICA DELLA CLASSE BOARD
        BoardWithPieces();

        Timer timer = new Timer(2000, e -> {
            showAtariScreen = false;
            repaint();
        });
        timer.setRepeats(false);
        timer.start();

        //PARTE DA FINIRE DEL MOUSE LISTENER ...
        // MOUSE LISTENER
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int col = (e.getX() - (getWidth() - BOARD_PIXEL_SIZE) / 2) / TILE_SIZE;
                int row = (e.getY() - (getHeight() - BOARD_PIXEL_SIZE) / 2) / TILE_SIZE;

                if (selectedRow == -1 && selectedCol == -1) {
                    // PRENDO IL PEZZO
                    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                        selectedRow = row;
                        selectedCol = col;
                        repaint();
                    }
                } else {
                    // LO MUOVO SOLO SE LA NUOVA POSIZIONE È DIVERSA
                    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                        if (row != selectedRow || col != selectedCol) {
                            logicalBoard.move(new Position(selectedRow, selectedCol), new Position(row, col));
                            // AGGIORNO LE IMMAGINI
                            pieceImages[row][col] = pieceImages[selectedRow][selectedCol];
                            pieceImages[selectedRow][selectedCol] = null;
                            // ORA DESELEZIONO IL PEZZO PRESO--
                            selectedRow = -1;
                            selectedCol = -1;
                            repaint();
                        }
                    }
                }
            }
        });
    }

    private void loadAtari() {
        try {
            atariLogo = ImageIO.read(new File("assets/atari.jpg"));
        } catch (Exception e) {
            System.out.println("Failed to load Atari startup! ERROR:1");
        }
    }

    // metodo che carico tutte le immagini per la scacchiera
    private void loadPieceImages() {
        String[] colors = {"white", "black"};
        String[] pieces = {"pawn", "rook", "knight", "bishop", "queen", "king"};

        for (int c = 0; c < colors.length; c++) {
            for (int p = 0; p < pieces.length; p++) {
                try {
                    String filename = String.format("assets/%s_%s.png", colors[c], pieces[p]);
                    BufferedImage img = ImageIO.read(new File(filename));
                    pieceImageMap.put(colors[c] + "_" + pieces[p], img);
                } catch (Exception e) {
                    System.out.println("Failed to load image for " + colors[c] + " " + pieces[p] + "ERRORE: 2");
                }
            }
        }
    }

    // Creo la scacchiera iniziale (SOLO INIZIALE SENZA EVENTI)
    private void BoardWithPieces() {
        loadPieceImages(); //chiamo il metoodo per i singoli tasselli

        // CREO UN FOR SOLO DEI PEDONI ! (prima riga nera, ultima bianca)
        for (int i = 0; i < BOARD_SIZE; i++) {
            pieceImages[1][i] = pieceImageMap.get("black_pawn");
            pieceImages[6][i] = pieceImageMap.get("white_pawn");
        }

        // TORRE -- CAVALLO -- ALFIERE -- REGINA -- RE (reverse)
        String[] order = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
        for (int i = 0; i < BOARD_SIZE; i++) {
            pieceImages[0][i] = pieceImageMap.get("black_" + order[i]);
            pieceImages[7][i] = pieceImageMap.get("white_" + order[i]);
        }
    }

    // Disegno pannello
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showAtariScreen) {
            drawAtariScreen(g);
        } else {
            drawBackground(g);  // sfondo sfumato
            drawPixelBorder(g);  // bordo della scacchiera
            drawChessBoard(g);   // scacchiera
            drawPieces(g);        // pezzi
        }

        // Disegna il bordo di selezione del pezzo
        if (selectedRow != -1 && selectedCol != -1) {
            g.setColor(Color.RED);
            int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2 + selectedCol * TILE_SIZE;
            int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2 + selectedRow * TILE_SIZE;
            g.drawRect(startX, startY, TILE_SIZE, TILE_SIZE);
        }
    }
    // SCHERMATA ATARI
    private void drawAtariScreen(Graphics g) {
        if (atariLogo != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            double scaleX = (double) panelWidth / atariLogo.getWidth();
            double scaleY = (double) panelHeight / atariLogo.getHeight();
            double scale = Math.min(scaleX, scaleY);

            int newWidth = (int) (atariLogo.getWidth() * scale);
            int newHeight = (int) (atariLogo.getHeight() * scale);

            int x = (panelWidth - newWidth) / 2;
            int y = (panelHeight - newHeight) / 2;
            g.drawImage(atariLogo, x, y, newWidth, newHeight, this);
        } else {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Errore nel caricamento del logo Atari.", 50, 50);
        }
    }


    // SFONDO
    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                float ratio = (float) y / height;
                int r = (int) (180 * (1 - ratio) + 13 * ratio);
                int gr = (int) (220 * (1 - ratio) + 33 * ratio);
                int b = (int) (255 * (1 - ratio) + 66 * ratio);
                g2d.setColor(new Color(r, gr, b));
                g2d.fillRect(x, y, 4, 4);
            }
        }
    }

    // BORDO SCACCHIERA
    private void drawPixelBorder(Graphics g) {
        g.setColor(new Color(30, 18, 48)); // #1e1230

        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        int endX = startX + BOARD_PIXEL_SIZE;
        int endY = startY + BOARD_PIXEL_SIZE;
        int pixelSize = 10;

        for (int x = startX - pixelSize; x < endX + pixelSize; x += pixelSize) {
            g.fillRect(x, startY - pixelSize, pixelSize, pixelSize);
            g.fillRect(x, endY, pixelSize, pixelSize);
        }

        for (int y = startY; y < endY; y += pixelSize) {
            g.fillRect(startX - pixelSize, y, pixelSize, pixelSize);
            g.fillRect(endX, y, pixelSize, pixelSize);
        }
    }

    // SCACCHIERA GUI
    private void drawChessBoard(Graphics g) {
        Color dark = new Color(0x18254a);
        Color light = new Color(245, 238, 220);

        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isDark = (row + col) % 2 != 0;
                if (isDark) {
                    g.setColor(dark);
                } else {
                    g.setColor(light);
                }
                g.fillRect(startX + col * TILE_SIZE, startY + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    // PEZZI SCACCHIERA
    private void drawPieces(Graphics g) {
        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        double scaleFactor = 0.85;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                BufferedImage piece = pieceImages[row][col];
                if (piece != null) {
                    int pieceWidth = (int) (TILE_SIZE * scaleFactor);
                    int pieceHeight = (int) (TILE_SIZE * scaleFactor);
                    BufferedImage resizedPiece = resizeImage(piece, pieceWidth, pieceHeight);
                    int x = startX + col * TILE_SIZE + (TILE_SIZE - pieceWidth) / 2;
                    int y = startY + row * TILE_SIZE + (TILE_SIZE - pieceHeight) / 2;
                    g.drawImage(resizedPiece, x, y, null);
                }
            }
        }
    }

    // DA FINIRE (??)
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedResizedImage.createGraphics();
        g2d.drawImage(resizedImage, 0, 0, null);
        g2d.dispose();
        return bufferedResizedImage;
    }

    // Avvio la GUI --> main
    public static void main(String[] args) {
        JFrame frame = new JFrame("PRIMA PROVA GUI");
        Main gui = new Main();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(gui);
        frame.setVisible(true);
    }
}
