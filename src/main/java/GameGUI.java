package src.main.java;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class GameGUI extends JPanel {

    private static final boolean SKIP_BACKGROUND = false;

    private List<Position> cachedSelectionMoves = Collections.emptyList();

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private static final int BOARD_PIXEL_SIZE = TILE_SIZE * BOARD_SIZE;

    private Piece selectedPiece = null;
    private Color highlightColor = null;
    private GameLogic gameLogic;
    private boolean showAtariScreen = true;
    private boolean showNameInput = false;
    private BufferedImage atariLogo;
    private int selectedRow = -1, selectedCol = -1;
    private boolean gameOver = false;
    private ChessColor winner = null;
    private List<Piece> whiteCaptured = new ArrayList<>();
    private List<Piece> blackCaptured = new ArrayList<>();
    private String playerWhiteName = "White Player";
    private String playerBlackName = "Black Player";
    private boolean isDraw = false;
    private String drawReason = "";

    // Cache scaling pezzi
    private final Map<String, Image> pieceScaleCache = new HashMap<>();

    // Background prerender
    private BufferedImage backgroundCache = null;
    private Dimension backgroundCacheSize = new Dimension(0,0);
    private final Map<String, BufferedImage> assetImageCache = new HashMap<>();

    /* ================== STELLE (campo puntiforme) ================== */
    private final List<Point> tinyStars = new ArrayList<>();
    private final List<Point> smallStars = new ArrayList<>();
    private final List<Point> mediumStars = new ArrayList<>();

    // ==== CONFIG STELLE PUNTIFORMI ====
    private static final int TINY_COUNT = 150;
    private static final int SMALL_COUNT = 80;
    private static final int MEDIUM_COUNT = 32;
    private static final int MIN_DIST = 12;
    private static final Color STAR_COLOR_TINY = new Color(255,255,255,140);
    private static final Color STAR_COLOR_SMALL = new Color(255,235,200,180);
    private static final Color STAR_COLOR_MEDIUM = new Color(200,225,255,210);

    /* ================== STELLE IMMAGINE (senza overlap) ================== */
    private final List<StarSprite> imageStars = new ArrayList<>();

    // ==== CONFIG STELLE IMMAGINE ====
    private static final int IMAGE_STAR_COUNT = 18;
    private static final int MIN_GAP = 6; // padding minimo tra sprite immagine
    private static final int LEFT_MARGIN_EXTRA = 10;
    private static final String[] STAR_ASSETS = {
            "star1.png", "starunique.png", "starunique2.png"
    };

    /* ================== TIMER CHESS ================== */
    private long whiteMillis = 5 * 60 * 1000; // default 5:00
    private long blackMillis = 5 * 60 * 1000;
    private static final long START_MILLIS_DEFAULT = 5 * 60 * 1000;
    private static final long INCREMENT_MILLIS = 0; // es. 2000 per +2s
    private javax.swing.Timer clockSwingTimer;
    private long lastTickNano = 0L;
    private boolean clocksRunning = false;
    private boolean lossOnTime = true;

    public GameGUI() {
        loadAtari();
        gameLogic = new GameLogic();
        loadFontOnce();

        initClocks();

        Timer atariTimer = new Timer(2000, e -> {
            showAtariScreen = false;
            showNameInput = true;
            getPlayerNames();
            repaint();
        });
        atariTimer.setRepeats(false);
        atariTimer.start();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (gameOver) {
                    resetGame();
                    return;
                }
                if (showAtariScreen || showNameInput) return;

                final int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
                final int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;

                int col = (e.getX() - startX) / TILE_SIZE;
                int row = (e.getY() - startY) / TILE_SIZE;

                if (col < 0 || col >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE) return;
                handleSelectionClick(row, col);
            }
        });
    }

    /* ===================== Init / Reset ===================== */

    private void loadFontOnce() {
        try (InputStream fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf")) {
            if (fontStream != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            }
        } catch (Exception ignored) {}
    }

    private void loadAtari() {
        try {
            atariLogo = ImageIO.read(new File("assets/atari.jpg"));
        } catch (Exception ignored) {}
    }

    private void resetGame() {
        gameLogic = new GameLogic();

        // Ricrea nuove liste mutabili (non usare clear() su possibili liste immutabili)
        whiteCaptured = new ArrayList<>();
        blackCaptured = new ArrayList<>();

        gameOver = false;
        winner = null;
        isDraw = false;
        drawReason = "";

        clearSelection();

        // Reset timer
        whiteMillis = blackMillis = START_MILLIS_DEFAULT;
        lastTickNano = System.nanoTime();
        clocksRunning = true;

        cachedSelectionMoves = Collections.emptyList();

        repaint();
    }

    /* ================= Selezione ottimizzata ================= */

    private void handleSelectionClick(int row, int col) {
        if (selectedRow < 0) {
            Piece p = gameLogic.getBoard().getPieceAt(new Position(row, col));
            if (p != null && p.getColor() == gameLogic.getTurn()) {
                selectedPiece = p;
                selectedRow = row;
                selectedCol = col;
                // Colore azzurrino brillante (bordo)
                highlightColor = new Color(70, 200, 255, 230);
                computeSelectionMoves(row, col, p);
                repaint();
            }
        } else {
            if (row == selectedRow && col == selectedCol) {
                clearSelection();
                repaint();
                return;
            }
            boolean moved = gameLogic.move(new Position(selectedRow, selectedCol), new Position(row, col));

            if (moved) {
                // Incremento sul giocatore che ha appena mosso (turn già aggiornato dentro gameLogic)
                if (INCREMENT_MILLIS > 0) {
                    ChessColor justMoved = (gameLogic.getTurn() == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;
                    if (justMoved == ChessColor.WHITE) whiteMillis += INCREMENT_MILLIS;
                    else blackMillis += INCREMENT_MILLIS;
                }
            }

            clearSelection();
            if (moved) {
                updateCapturedPieces();
                checkGameEnd();
                if (gameOver) clocksRunning = false;
            }
            repaint();
        }
    }

    private void computeSelectionMoves(int row, int col, Piece piece) {
        List<Position> geo = piece.getValidPositions();
        if (geo.isEmpty()) {
            cachedSelectionMoves = Collections.emptyList();
            return;
        }
        ArrayList<Position> legal = new ArrayList<>(geo.size());
        Position from = new Position(row, col);
        for (Position candidate : geo) {
            if (candidate.x < 0 || candidate.x >= BOARD_SIZE ||
                    candidate.y < 0 || candidate.y >= BOARD_SIZE)
                continue;
            if (gameLogic.isMoveValid(from, candidate, true)) {
                legal.add(candidate);
            }
        }
        cachedSelectionMoves = legal;
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        selectedPiece = null;
        highlightColor = null;
        cachedSelectionMoves = Collections.emptyList();
    }

    /* =================== Stato di gioco ===================== */

    private void updateCapturedPieces() {
        // Assunzione: getCapturedPieces(color) restituisce i pezzi DI quel colore che sono stati catturati.
        List<Piece> capturedBlack = gameLogic.getCapturedPieces(ChessColor.BLACK);
        List<Piece> capturedWhite = gameLogic.getCapturedPieces(ChessColor.WHITE);

        whiteCaptured = (capturedBlack == null) ? new ArrayList<>() : new ArrayList<>(capturedBlack);
        blackCaptured = (capturedWhite == null) ? new ArrayList<>() : new ArrayList<>(capturedWhite);
    }

    private void checkGameEnd() {
        if (gameLogic.isCheckmate()) {
            gameOver = true;
            winner = (gameLogic.getTurn() == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;
        } else if (gameLogic.isDraw()) {
            gameOver = true;
            isDraw = true;
            drawReason = gameLogic.getDrawReason();
        }
    }

    /* ================= Nome giocatori ================= */

    private void getPlayerNames() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        Font customFont;
        try (InputStream fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf")) {
            customFont = (fontStream != null)
                    ? Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(14f)
                    : new Font("Arial", Font.BOLD, 14);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 14);
        }

        JLabel whiteLabel = new JLabel("WHITE PLAYER NAME:");
        whiteLabel.setFont(customFont);
        whiteLabel.setForeground(Color.WHITE);
        panel.add(whiteLabel);

        JTextField whiteField = new JTextField("Player 1", 15);
        styleTextField(whiteField, customFont);
        panel.add(whiteField);

        JLabel blackLabel = new JLabel("BLACK PLAYER NAME:");
        blackLabel.setFont(customFont);
        blackLabel.setForeground(Color.WHITE);
        panel.add(blackLabel);

        JTextField blackField = new JTextField("Player 2", 15);
        styleTextField(blackField, customFont);
        panel.add(blackField);

        JButton okButton = new JButton("START GAME");
        styleButton(okButton, customFont);
        okButton.addActionListener(e -> {
            playerWhiteName = whiteField.getText().trim().isEmpty() ? "White Player" : whiteField.getText();
            playerBlackName = blackField.getText().trim().isEmpty() ? "Black Player" : blackField.getText();
            showNameInput = false;
            Window w = SwingUtilities.getWindowAncestor(panel);
            if (w != null) w.dispose();
            repaint();
        });

        panel.add(new JLabel());
        panel.add(okButton);

        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void styleTextField(JTextField field, Font font) {
        field.setFont(font);
        field.setBackground(new Color(255, 255, 255, 200));
        field.setBorder(BorderFactory.createLineBorder(new Color(24, 53, 99), 3));
        field.setForeground(Color.BLACK);
    }

    private void styleButton(JButton button, Font font) {
        button.setFont(font);
        button.setBackground(new Color(24, 53, 99));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setFocusPainted(false);
    }

    /* ===================== Painting ===================== */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showAtariScreen) {
            drawAtariScreen(g);
        } else if (showNameInput) {
            drawBackground(g);
        } else if (gameOver) {
            drawBackground(g);
            if (isDraw) drawDrawScreen(g); else drawWinScreen(g);
        } else {
            drawGame(g);
        }
    }

    private void drawGame(Graphics g) {
        drawBackground(g);
        drawPixelBorder(g);
        drawChessBoard(g);
        drawPieces(g);
        drawCapturedPieces(g);
        drawSelectionHighlight(g);
        drawMoveHints(g);
    }

    private void drawSelectionHighlight(Graphics g) {
        if (selectedRow < 0) return;
        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2 + selectedCol * TILE_SIZE;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2 + selectedRow * TILE_SIZE;

        if (highlightColor == null) {
            highlightColor = new Color(70, 200, 255, 230);
        }

        // Riempimento tenue
        g.setColor(new Color(highlightColor.getRed(), highlightColor.getGreen(), highlightColor.getBlue(), 65));
        g.fillRect(startX, startY, TILE_SIZE, TILE_SIZE);

        Graphics2D g2 = (Graphics2D) g;

        // Bordo spesso
        g2.setColor(highlightColor);
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRect(startX, startY, TILE_SIZE, TILE_SIZE);

        // Glow interno
        g2.setColor(new Color(255,255,255,120));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(startX+2, startY+2, TILE_SIZE-4, TILE_SIZE-4);
    }

    private void drawMoveHints(Graphics g) {
        if (cachedSelectionMoves.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g;
        int boardStartX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int boardStartY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        // Viola (puoi cambiare qui se preferisci azzurro)
        g2.setColor(new Color(210, 120, 255, 170));
        int r = TILE_SIZE / 3;
        for (Position p : cachedSelectionMoves) {
            int x = boardStartX + p.y * TILE_SIZE;
            int y = boardStartY + p.x * TILE_SIZE;
            g2.fillOval(x + (TILE_SIZE - r) / 2, y + (TILE_SIZE - r) / 2, r, r);
        }
    }

    private void drawAtariScreen(Graphics g) {
        if (atariLogo != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            double scale = Math.min(panelWidth / (double) atariLogo.getWidth(),
                    panelHeight / (double) atariLogo.getHeight());
            int newWidth = (int) (atariLogo.getWidth() * scale);
            int newHeight = (int) (atariLogo.getHeight() * scale);
            int x = (panelWidth - newWidth) / 2;
            int y = (panelHeight - newHeight) / 2;
            g.drawImage(atariLogo, x, y, newWidth, newHeight, this);
        }
    }

    /* =========== BACKGROUND PRERENDERIZZATO =========== */

    private void drawBackground(Graphics g) {
        if (SKIP_BACKGROUND) {
            g.setColor(new Color(30, 30, 40));
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        if (backgroundNeedsRebuild()) {
            buildBackgroundCache();
        }
        if (backgroundCache != null) {
            g.drawImage(backgroundCache, 0, 0, null);
        }
    }

    private boolean backgroundNeedsRebuild() {
        return backgroundCache == null
                || backgroundCacheSize.width != getWidth()
                || backgroundCacheSize.height != getHeight();
    }

    private void buildBackgroundCache() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        backgroundCache = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        backgroundCacheSize.setSize(w, h);

        Graphics2D g2 = backgroundCache.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Base gradient
        g2.setPaint(new GradientPaint(0, 0, new Color(15, 15, 35), 0, h, new Color(40, 25, 55)));
        g2.fillRect(0, 0, w, h);

        // Glow diffuso
        RadialGradientPaint glow = new RadialGradientPaint(
                new Point(w/3, h/2),
                Math.min(w, h)/1.5f,
                new float[]{0f, 1f},
                new Color[]{new Color(90,90,140,80), new Color(20,20,40,0)}
        );
        g2.setPaint(glow);
        g2.fillRect(0,0,w,h);

        int boardStartX = (w - BOARD_PIXEL_SIZE) / 2;

        // Oggetti principali
        drawAsset(g2, "moon.png", 20, 30, 220, 170);
        drawAsset(g2, "planet.png", boardStartX / 2 - 40, h / 2 - 90, 175, 135);
        drawAsset(g2, "planet.png", 25, h - 210, 150, 115);

        // Comete originali
        drawAsset(g2, "cometa.png", 70, 220, 60, 60);
        drawAsset(g2, "cometa3.png", 170, 360, 50, 50);

        // Comete aggiuntive (senza posizionarle sulla luna)
        drawAssetFlip(g2, "cometa.png", 110, 500, 55, 55);
        drawAssetFlip(g2, "cometa3.png", 60, h - 260, 70, 70);

        // Stella grande nuova vicino al bordo scacchiera
        int bigStarW = 125;
        int bigStarH = 80;
        int bigStarX = boardStartX - bigStarW - 18;
        int bigStarY = h - bigStarH - 55;
        drawAsset(g2, "starunique2.png", bigStarX, bigStarY, bigStarW, bigStarH);

        // Rigenera campo stelle
        initStarField(boardStartX, h);

        // Stelle puntiformi
        g2.setColor(STAR_COLOR_TINY);
        for (Point p : tinyStars) g2.fillRect(p.x, p.y, 2, 2);

        g2.setColor(STAR_COLOR_SMALL);
        for (Point p : smallStars) g2.fillOval(p.x, p.y, 3, 3);

        for (Point p : mediumStars) {
            Paint prev = g2.getPaint();
            RadialGradientPaint rg = new RadialGradientPaint(
                    new Point(p.x+3, p.y+3),
                    6,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255,255,255,210), new Color(255,255,255,0)}
            );
            g2.setPaint(rg);
            g2.fillOval(p.x, p.y, 6, 6);
            g2.setPaint(prev);
        }

        // Stelle immagine (già non sovrapposte)
        for (StarSprite s : imageStars) {
            BufferedImage img = loadAsset(s.asset);
            if (img != null) {
                int crop = Math.min(5, Math.min(img.getWidth()/10, img.getHeight()/10));
                BufferedImage sub = img.getSubimage(crop, crop,
                        img.getWidth()-crop*2, img.getHeight()-crop*2);
                g2.drawImage(sub, s.x, s.y, s.w, s.h, null);
            }
        }

        g2.dispose();
    }

    /**
     * Genera un campo di stelle nella fascia sinistra (prima della scacchiera),
     * con distribuzione semi-uniforme e sprite immagine non sovrapposti.
     */
    private void initStarField(int boardStartX, int height) {
        tinyStars.clear();
        smallStars.clear();
        mediumStars.clear();
        imageStars.clear();

        int areaWidth = boardStartX - LEFT_MARGIN_EXTRA;
        if (areaWidth < 140) return;

        Random rnd = new Random(4321);

        // ---- Stelle puntiformi (tiny) con distanza minima ----
        java.util.function.Predicate<Point> farEnough = p -> {
            for (Point q : tinyStars) {
                int dx = p.x - q.x;
                int dy = p.y - q.y;
                if (dx*dx + dy*dy < MIN_DIST*MIN_DIST) return false;
            }
            return true;
        };
        int attempts = 0;
        while (tinyStars.size() < TINY_COUNT && attempts < TINY_COUNT * 30) {
            attempts++;
            int x = 5 + rnd.nextInt(Math.max(1, areaWidth - 10));
            int y = 15 + rnd.nextInt(Math.max(1, height - 30));
            Point p = new Point(x, y);
            if (farEnough.test(p)) tinyStars.add(p);
        }

        // Small & Medium
        for (int i = 0; i < SMALL_COUNT; i++) {
            int x = 5 + rnd.nextInt(areaWidth - 10);
            int y = 10 + rnd.nextInt(height - 20);
            smallStars.add(new Point(x,y));
        }
        for (int i = 0; i < MEDIUM_COUNT; i++) {
            int x = 15 + rnd.nextInt(areaWidth - 30);
            int y = 25 + rnd.nextInt(height - 50);
            mediumStars.add(new Point(x,y));
        }

        // ---- Stelle immagine senza overlapping ----
        int maxTries = IMAGE_STAR_COUNT * 40;
        int created = 0;
        int tries = 0;
        while (created < IMAGE_STAR_COUNT && tries < maxTries) {
            tries++;
            String asset = STAR_ASSETS[rnd.nextInt(STAR_ASSETS.length)];
            int w = pickWidth(rnd);
            double ratio = 0.70 + rnd.nextDouble()*0.10;
            int h = (int)Math.round(w * ratio);

            int x = 8 + rnd.nextInt(Math.max(1, areaWidth - w - 16));
            int y = 8 + rnd.nextInt(Math.max(1, height - h - 24));

            Rectangle candidate = new Rectangle(x - MIN_GAP/2, y - MIN_GAP/2, w + MIN_GAP, h + MIN_GAP);

            boolean collides = false;
            for (StarSprite other : imageStars) {
                if (candidate.intersects(other.boundsWithPadding(MIN_GAP))) {
                    collides = true;
                    break;
                }
            }
            if (collides) continue;

            imageStars.add(new StarSprite(asset, x, y, w, h));
            created++;
        }
    }

    private int pickWidth(Random rnd) {
        double roll = rnd.nextDouble();
        if (roll < 0.40) {
            return 18 + rnd.nextInt(7);
        } else if (roll < 0.75) {
            return 26 + rnd.nextInt(9);
        } else if (roll < 0.92) {
            return 36 + rnd.nextInt(10);
        } else {
            return 52 + rnd.nextInt(10);
        }
    }

    private void drawAsset(Graphics2D g2, String file, int x, int y, int w, int h) {
        BufferedImage img = loadAsset(file);
        if (img == null) return;
        int crop = Math.min(5, Math.min(img.getWidth()/10, img.getHeight()/10));
        BufferedImage sub = img.getSubimage(crop, crop,
                img.getWidth()-crop*2, img.getHeight()-crop*2);
        g2.drawImage(sub, x, y, w, h, null);
    }

    /** Disegna un asset flippato orizzontalmente */
    private void drawAssetFlip(Graphics2D g2, String file, int x, int y, int w, int h) {
        BufferedImage img = loadAsset(file);
        if (img == null) return;
        int crop = Math.min(5, Math.min(img.getWidth()/10, img.getHeight()/10));
        BufferedImage sub = img.getSubimage(crop, crop,
                img.getWidth()-crop*2, img.getHeight()-crop*2);
        g2.drawImage(sub, x + w, y, -w, h, null);
    }

    private BufferedImage loadAsset(String file) {
        BufferedImage cached = assetImageCache.get(file);
        if (cached != null) return cached;
        for (String path : loadAssetPathCandidates(file)) {
            try {
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    assetImageCache.put(file, img);
                    return img;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private List<String> loadAssetPathCandidates(String file) {
        return Arrays.asList(
                "assets/" + file,
                "resources/assets/" + file,
                file
        );
    }

    /* ===================== Altre parti UI ===================== */

    private void drawPixelBorder(Graphics g) {
        g.setColor(new Color(30, 18, 48));
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

    private void drawChessBoard(Graphics g) {
        final Color dark = new Color(0x18254a);
        final Color light = new Color(245, 238, 220);
        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        for (int row = 0; row < BOARD_SIZE; row++) {
            int y = startY + row * TILE_SIZE;
            for (int col = 0; col < BOARD_SIZE; col++) {
                g.setColor(((row + col) & 1) == 1 ? dark : light);
                g.fillRect(startX + col * TILE_SIZE, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        final double scaleFactor = 0.85;
        final int pieceW = (int) (TILE_SIZE * scaleFactor);
        final int pieceH = pieceW;
        for (int r = 0; r < BOARD_SIZE; r++) {
            int y = startY + r * TILE_SIZE + (TILE_SIZE - pieceH) / 2;
            for (int c = 0; c < BOARD_SIZE; c++) {
                Piece p = gameLogic.getBoard().getPieceAt(new Position(r, c));
                if (p == null) continue;
                BufferedImage tex = p.getTexture();
                Image scaled = getScaledPieceImage(tex, pieceW, pieceH);
                if (scaled == null) {
                    g.setColor(p.getColor() == ChessColor.WHITE ? Color.WHITE : Color.BLACK);
                    g.fillOval(startX + c * TILE_SIZE + 10, y + 10, pieceW - 20, pieceH - 20);
                } else {
                    int x = startX + c * TILE_SIZE + (TILE_SIZE - pieceW) / 2;
                    g.drawImage(scaled, x, y, null);
                }
            }
        }
    }

    private void drawCapturedPieces(Graphics g) {
        int iconSize = 40;
        int padding = 8;
        int windowWidth = 250;
        int marginFromBoard = 35;
        int boardStartX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int boardStartY = (getHeight() - BOARD_PIXEL_SIZE) / 2;

        int windowX = boardStartX + BOARD_PIXEL_SIZE + marginFromBoard;
        int windowY = boardStartY;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(windowX, windowY, windowWidth, BOARD_PIXEL_SIZE, 20, 20);

        g2d.setColor(new Color(24, 53, 99));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(windowX, windowY, windowWidth, BOARD_PIXEL_SIZE, 20, 20);

        g2d.setFont(new Font("Press Start 2P", Font.PLAIN, 12));
        int textLeft = windowX + 15;

        /* ===== TIMER WHITE (in alto) ===== */
        int timerHeight = 26;
        drawPlayerClock(g2d,
                playerWhiteName,
                formatClock(whiteMillis),
                windowX + 10,
                windowY + 8,
                windowWidth - 20,
                timerHeight,
                gameLogic.getTurn() == ChessColor.WHITE && !gameOver);

        /* ===== Label & pezzi White ===== */
        int whiteLabelY = windowY + 8 + timerHeight + 18;
        g2d.setColor(Color.BLACK);
        g2d.drawString(playerWhiteName + " captured:", textLeft, whiteLabelY);
        int whitePiecesStartY = whiteLabelY + 10;

        for (int i = 0; i < whiteCaptured.size(); i++) {
            BufferedImage img = whiteCaptured.get(i).getTexture();
            if (img != null) {
                Image scaled = getScaledPieceImage(img, iconSize, iconSize);
                int x = textLeft + (i % 5) * (iconSize + padding);
                int y = whitePiecesStartY + (i / 5) * (iconSize + padding);
                g2d.drawImage(scaled, x, y, null);
            }
        }

        /* ===== Linea divisoria ===== */
        int midLineY = windowY + BOARD_PIXEL_SIZE / 2;
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawLine(windowX + 10, midLineY, windowX + windowWidth - 10, midLineY);

        /* ===== TIMER BLACK (subito sopra sezione nera) ===== */
        int blackTimerY = midLineY + 8;
        drawPlayerClock(g2d,
                playerBlackName,
                formatClock(blackMillis),
                windowX + 10,
                blackTimerY,
                windowWidth - 20,
                timerHeight,
                gameLogic.getTurn() == ChessColor.BLACK && !gameOver);

        /* ===== Label & pezzi Black ===== */
        int blackLabelY = blackTimerY + timerHeight + 18;
        g2d.setColor(Color.BLACK);
        g2d.drawString(playerBlackName + " captured:", textLeft, blackLabelY);
        int blackPiecesStartY = blackLabelY + 10;

        for (int i = 0; i < blackCaptured.size(); i++) {
            BufferedImage img = blackCaptured.get(i).getTexture();
            if (img != null) {
                Image scaled = getScaledPieceImage(img, iconSize, iconSize);
                int x = textLeft + (i % 5) * (iconSize + padding);
                int y = blackPiecesStartY + (i / 5) * (iconSize + padding);
                g2d.drawImage(scaled, x, y, null);
            }
        }
    }

    private void drawDrawScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        try (InputStream fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf")) {
            Font customFont = (fontStream != null)
                    ? Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(48f)
                    : new Font("Arial", Font.BOLD, 48);
            g2d.setFont(customFont);
        } catch (Exception e) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
        }

        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String drawText = "DRAW!";
        int textWidth = fm.stringWidth(drawText);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2 - 50;
        g2d.drawString(drawText, x, y);

        g2d.setFont(g2d.getFont().deriveFont(24f));
        fm = g2d.getFontMetrics();
        String reasonText = "(" + drawReason + ")";
        textWidth = fm.stringWidth(reasonText);
        x = (getWidth() - textWidth) / 2;
        y = getHeight() / 2 + 10;
        g2d.drawString(reasonText, x, y);

        String restartText = "Click anywhere to restart";
        textWidth = fm.stringWidth(restartText);
        x = (getWidth() - textWidth) / 2;
        y = getHeight() / 2 + 50;
        g2d.drawString(restartText, x, y);
    }

    private void drawWinScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        String winText = (winner == ChessColor.WHITE)
                ? playerWhiteName + " WINS!"
                : playerBlackName + " WINS!";

        try (InputStream fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf")) {
            Font customFont = (fontStream != null)
                    ? Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(48f)
                    : new Font("Arial", Font.BOLD, 48);
            g2d.setFont(customFont);
        } catch (Exception e) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
        }

        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(winText);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;
        g2d.drawString(winText, x, y);

        g2d.setFont(new Font(g2d.getFont().getName(), Font.PLAIN, 24));
        String restartText = "Click anywhere to restart";
        textWidth = g2d.getFontMetrics().stringWidth(restartText);
        x = (getWidth() - textWidth) / 2;
        y = getHeight() / 2 + 50;
        g2d.drawString(restartText, x, y);
    }

    /* ============== Utils scaling pezzi ============== */
    private Image getScaledPieceImage(BufferedImage img, int w, int h) {
        if (img == null) return null;
        String key = System.identityHashCode(img) + "@" + w + "x" + h;
        Image cached = pieceScaleCache.get(key);
        if (cached != null) return cached;
        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        pieceScaleCache.put(key, scaled);
        return scaled;
    }

    /* ================== TIMER METHODS ================== */
    private void initClocks() {
        whiteMillis = blackMillis = START_MILLIS_DEFAULT;
        lastTickNano = System.nanoTime();
        clockSwingTimer = new javax.swing.Timer(200, e -> updateClocks());
        clockSwingTimer.start();
        clocksRunning = true;
    }

    private void updateClocks() {
        if (!clocksRunning || gameOver || showAtariScreen || showNameInput) return;

        long now = System.nanoTime();
        long delta = now - lastTickNano;
        lastTickNano = now;
        long deltaMs = Math.max(0, delta / 1_000_000L);

        ChessColor turn = gameLogic.getTurn();
        if (turn == ChessColor.WHITE) {
            whiteMillis -= deltaMs;
            if (whiteMillis < 0) whiteMillis = 0;
        } else {
            blackMillis -= deltaMs;
            if (blackMillis < 0) blackMillis = 0;
        }

        if (lossOnTime && (whiteMillis == 0 || blackMillis == 0) && !gameOver) {
            gameOver = true;
            winner = (whiteMillis == 0) ? ChessColor.BLACK : ChessColor.WHITE;
            clocksRunning = false;
        }

        repaint();
    }

    private String formatClock(long ms) {
        if (ms < 0) ms = 0;
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (totalSeconds < 10) {
            long tenths = (ms % 1000) / 100;
            return String.format("%d:%02d.%d", minutes, seconds, tenths);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    private void drawPlayerClock(Graphics2D g2d, String name, String timeText,
                                 int x, int y, int w, int h, boolean active) {
        Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g2d.setColor(new Color(250, 248, 244, 235));
        g2d.fillRoundRect(x, y, w, h, 10, 10);

        // Colori gradient
        Color c1, c2, c3;
        if (active) {
            c1 = new Color(120, 235, 255);
            c2 = new Color(50, 160, 250);
            c3 = new Color(140, 250, 255);
            long activeMs = name.equals(playerWhiteName) ? whiteMillis : blackMillis;
            if (activeMs <= 10_000) { // Low time alert
                c1 = new Color(255, 140, 140);
                c2 = new Color(200, 40, 60);
                c3 = new Color(255, 180, 180);
            }
        } else {
            c1 = new Color(170, 150, 210);
            c2 = new Color(120, 100, 170);
            c3 = new Color(190, 170, 225);
        }

        LinearGradientPaint grad = new LinearGradientPaint(
                new Point(x, y),
                new Point(x + w, y + h),
                new float[]{0f, 0.5f, 1f},
                new Color[]{c1, c2, c3}
        );

        Stroke oldStroke = g2d.getStroke();
        Paint oldPaint = g2d.getPaint();

        g2d.setStroke(new BasicStroke(active ? 4.2f : 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(grad);
        g2d.drawRoundRect(x, y, w, h, 10, 10);

        // Inner glow
        g2d.setStroke(new BasicStroke(1.4f));
        g2d.setColor(new Color(255, 255, 255, active ? 140 : 90));
        g2d.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 8, 8);

        g2d.setStroke(oldStroke);
        g2d.setPaint(oldPaint);

        // Nome
        Font nameFont = new Font("Press Start 2P", Font.PLAIN, 11);
        g2d.setFont(nameFont);
        FontMetrics fm = g2d.getFontMetrics();
        String shortName = name;
        if (shortName.length() > 12) shortName = shortName.substring(0, 12);
        int nameX = x + 8;
        int nameY = y + (h + fm.getAscent()) / 2 - 3;
        g2d.setColor(new Color(40, 40, 60));
        g2d.drawString(shortName.toUpperCase(), nameX, nameY);

        // Tempo
        Font timeFont = new Font("Press Start 2P", Font.PLAIN, 12);
        g2d.setFont(timeFont);
        FontMetrics tfm = g2d.getFontMetrics();
        int timeX = x + w - tfm.stringWidth(timeText) - 10;
        int timeY = y + (h + tfm.getAscent()) / 2 - 4;
        g2d.setColor(active ? new Color(15, 55, 90) : new Color(70, 65, 95));
        g2d.drawString(timeText, timeX, timeY);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    /* ===== Helper per stelle decorative con immagine ===== */
    private static class StarSprite {
        final String asset;
        final int x,y,w,h;
        StarSprite(String a,int x,int y,int w,int h){this.asset=a;this.x=x;this.y=y;this.w=w;this.h=h;}
        Rectangle boundsWithPadding(int pad){
            return new Rectangle(x - pad, y - pad, w + pad*2, h + pad*2);
        }
    }
}
