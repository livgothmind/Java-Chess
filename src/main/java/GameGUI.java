package src.main.java;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class GameGUI extends JPanel {
    private final int TILE_SIZE = 80; // dimensione ogni casellina
    private final int BOARD_SIZE = 8; // ricordo che scacchiera è una 8x8
    private final int BOARD_PIXEL_SIZE = TILE_SIZE * BOARD_SIZE;

    private final GameLogic gameLogic;
    private boolean showAtariScreen = true;
    private BufferedImage atariLogo; // immagine per il logo inziale
    private int selectedRow = -1, selectedCol = -1; // posizioni del pezzo selezionato

    public GameGUI() {
        loadAtari();
        gameLogic = new GameLogic();

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
                    Piece selectedPiece = gameLogic.getBoard().getPieceAt(new Position(row, col));
                    if (    row >= 0 && row < BOARD_SIZE        &&
                            col >= 0 && col < BOARD_SIZE        &&
                            selectedPiece != null               &&
                            gameLogic.getTurn() == selectedPiece.getColor()) {
                        selectedRow = row;
                        selectedCol = col;
                        repaint();
                    }
                } else {
                    // LO MUOVO SOLO SE LA NUOVA POSIZIONE È DIVERSA
                    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                        gameLogic.move(new Position(selectedRow, selectedCol), new Position(row, col));
                        // DESELEZIONO IL PEZZO PRESO
                        selectedRow = -1;
                        selectedCol = -1;
                        repaint();
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

    // Disegno pannello
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showAtariScreen) {
            drawAtariScreen(g);
        } else {
            drawBackground(g);   // sfondo sfumato
            drawPixelBorder(g);  // bordo della scacchiera
            drawChessBoard(g);   // scacchiera
            drawPieces(g);       // pezzi
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
                Piece piece = gameLogic.getBoard().getPieceAt(new Position(row, col));
                if (piece != null) {
                    BufferedImage pieceImg = piece.getTexture();
                    int pieceWidth = (int) (TILE_SIZE * scaleFactor);
                    int pieceHeight = (int) (TILE_SIZE * scaleFactor);
                    BufferedImage resizedPiece = resizeImage(pieceImg, pieceWidth, pieceHeight);
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
}
