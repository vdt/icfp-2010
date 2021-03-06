package gategui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Rectangle2D;
import java.lang.String;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;

/**
 *
 */
public class GatePainter extends JPanel {

    final static int GATE_WIDTH = 80;
    final static int GATE_HEIGHT = 50;
    final static int INPUT_LINE = -1000;
    final static int OUTPUT_LINE = -2000;
    ArrayList<Gate> gates = new ArrayList<Gate>();
    Gate lastSelectedGate = null;
    Quadrant lastSelectedQuadrant = null;
    private final GateFrame frame;
    boolean inputLineDefinitionMode = false;
    boolean outputLineDefinitionMode = false;
    int inputLineGoesToGateNr = -1;
    int outputLineGoesToGateNr = -1;

    public enum Quadrant {

        TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT
    }

    public GatePainter(GateFrame frame) {
        addMouseListener(new GateMouseListener());
        this.frame = frame;
    }

    void addGate(Gate gate) {
        int gateNr = gates.size();
        // gate.rect = new Rectangle2D.Float((gateNr % 2 == 0) ? 80 : 180, gateNr/2 * 80, GATE_WIDTH, GATE_HEIGHT);
        gate.rect = new Rectangle2D.Float(80, gateNr * 80, GATE_WIDTH, GATE_HEIGHT);
        gates.add(gate);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Paint oldpaint = g2.getPaint();

        g2.setColor(Color.white);
        g2.fill3DRect(0, 0, (int) getSize().width, (int) getSize().height, true);

        int gateNr = -1;
        for (Gate gate : gates) {
            gateNr++;
            Rectangle2D.Float rect = gate.rect;


            g2.setColor(Color.green);
            g2.fill(gate.rect);

            g2.setColor(Color.blue);
            if (gate.inL != -1) {
                g2.drawString(gate.inL + "" + gate.inLc, (int) rect.getX() - 20, (int) rect.getY() + 10);
            }
            if (gate.inR != -1) {
                g2.drawString(gate.inR + "" + gate.inRc, (int) rect.getX() - 20, (int) rect.getY() + GATE_HEIGHT - 10);
            }
            if (gate.outL != -1) {
                g2.drawString(gate.outL + "" + gate.outLc, (int) rect.getX() + GATE_WIDTH + 10, (int) rect.getY() + 10);
            }
            if (gate.outR != -1) {
                g2.drawString(gate.outR + "" + gate.outRc, (int) rect.getX() + GATE_WIDTH + 10, (int) rect.getY() + GATE_HEIGHT - 10);
            }

            // gateNr label
            g2.setColor(Color.white);
            g2.drawString("gate " + gateNr, (int) rect.getX() + GATE_WIDTH / 2 - 20, (int) rect.getY() + GATE_HEIGHT / 2);

            // curves
            g2.setColor(Color.blue);
            g2.setStroke(new BasicStroke(2));
            if (gate.outL >= 0) {
                Gate gate2 = gates.get(gate.outL);
                CubicCurve2D c = new CubicCurve2D.Double();
                float y2 = (float) ((gate.outLc == 'L') ? gate2.getTopLeft().getY() : gate2.getBottomLeft().getY());
                c.setCurve(gate.getTopRight().getX(), gate.getTopRight().getY(),
                        gate.getTopRight().getX() + 50, gate.getTopRight().getY(),
                        gate2.rect.x - 50, y2,
                        gate2.rect.x, y2);
                g2.draw(c);
            }
            if (gate.outR >= 0) {
                Gate gate2 = gates.get(gate.outR);
                CubicCurve2D c = new CubicCurve2D.Double();
                float y2 = (float) ((gate.outRc == 'L') ? gate2.getTopLeft().getY() : gate2.getBottomLeft().getY());
                c.setCurve(gate.getBottomRight().getX(), gate.getBottomRight().getY(),
                        gate.getBottomRight().getX() + 50, gate.getBottomRight().getY(),
                        gate2.rect.x - 50, y2,
                        gate2.rect.x, y2);
                g2.draw(c);
            }
        }
        g2.setPaint(oldpaint);

    }

    Gate getGateByPosition(int x, int y) {

        for (Gate gate : gates) {
            if (gate.rect.contains(x, y)) {
                return gate;
            }
        }
        return null;
    }

    Quadrant getQuadrantByPosition(Gate gate, int x, int y) {
        Rectangle2D.Float rect = gate.rect;
        if (y < (rect.y + rect.height / 2)) {
            return ((x < (rect.x + rect.width / 2)) ? Quadrant.TOPLEFT : Quadrant.TOPRIGHT);
        } else {
            return ((x < (rect.x + rect.width / 2)) ? Quadrant.BOTTOMLEFT : Quadrant.BOTTOMRIGHT);

        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1000, 1000);
//        return new Dimension(100, 50);
    }

    void exportGatesToTextField() {
        Gate[] gatesArr = gates.toArray(new Gate[0]);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d%c:%n", inputLineGoesToGateNr,
                (gatesArr[inputLineGoesToGateNr].inL == INPUT_LINE) ? 'L' : 'R'));
        for (int i = 0; i < gatesArr.length; i++) {
            if (frame.lineNrsCheckBox.isSelected()) {
                sb.append(i + ": "); // linenr
            }

            Gate gate = gatesArr[i];
            if (gate.inL != -1) {
                sb.append(String.format("%d%c", gate.inL, gate.inLc));
            }
            if (gate.inR != -1) {
                sb.append(String.format("%d%c", gate.inR, gate.inRc));
            }
            if ((gate.inL != -1) || (gate.inR != -1) || (gate.outL != -1) || (gate.outR != -1)) {
                sb.append("0#");
            }
            if (gate.outL != -1) {
                sb.append(String.format("%d%c", gate.outL, gate.outLc));
            }
            if (gate.outR != -1) {
                sb.append(String.format("%d%c", gate.outR, gate.outRc));
            }
            sb.append(",\n");

        }

        sb.replace(sb.lastIndexOf(","), sb.length(), ":\n");

        sb.append(String.format("%d%c%n", outputLineGoesToGateNr,
                (gatesArr[outputLineGoesToGateNr].outL == OUTPUT_LINE) ? 'L' : 'R'));
        // encode input and output as 'X'
        String str = sb.toString();
        str = str.replace("-1000I", "X");
        str = str.replace("-2000O", "X");

        frame.jTextArea1.setText(str);
    }

     void parseTxt() {

        String txt = "3L:\n4R4L0#2L2R,\n3L3R0#4L3R,\n0L0R0#X4R,\nX1R0#1L1R,\n1L2R0#0R0L:\n2L";

        gates.clear();
//        String txt = frame.jTextArea1.getText().trim();
        String[] parts = txt.split(":\n");
        if (parts.length != 3) {
            throw new IllegalArgumentException("parts != 3");
        }
        String[] lines = parts[1].split("\n");
        System.out.format("contains %d gates%n", lines.length);

        for (int i = 0; i < lines.length; i++) {
            String[] sides = lines[i].split("0#");
            assert sides.length == 2;
            String[] inSpec = parseSide(sides[0]);
            String[] outSpec = parseSide(sides[1]);
            assert inSpec.length == 4;
            assert outSpec.length == 4;
            Gate gate = new Gate();
            addGate(gate);

            // inspec
            if (inSpec[1].equals("X")) {
                gate.inL = INPUT_LINE;
                gate.inLc = 'I';
                inputLineGoesToGateNr = i;
            } else {
                gate.inL = Integer.parseInt(inSpec[0]);
                gate.inLc = inSpec[1].charAt(0);
            }
            if (inSpec[3].equals("X")) {
                gate.inR = INPUT_LINE;
                gate.inRc = 'I';
                inputLineGoesToGateNr = i;
            } else {
                gate.inR = Integer.parseInt(inSpec[2]);
                gate.inRc = inSpec[3].charAt(0);
            }

            // outspec
            if (outSpec[1].equals("X")) {
                gate.outL = OUTPUT_LINE;
                gate.outLc = 'O';
                outputLineGoesToGateNr = i;
            } else {
                gate.outL = Integer.parseInt(outSpec[0]);
                gate.outLc = outSpec[1].charAt(0);
            }
            if (outSpec[3].equals("X")) {
                gate.outR = OUTPUT_LINE;
                gate.outRc = 'O';
                outputLineGoesToGateNr = i;
            } else {
                gate.outR = Integer.parseInt(outSpec[2]);
                gate.outRc = outSpec[3].charAt(0);
            }

            System.out.format("gate%d: %s%n", i, gate);
        }
        repaint(300);
    }

    String[] parseSide(String side) {
        String[] result = new String[4];
        Matcher matcher = Pattern.compile("([0-9]+[RL]|X)([0-9]+[RL]|X)").matcher(side);
        if (matcher.find()) {
            String g1 = matcher.group(1);
            String g2 = matcher.group(2);
            if (g1.equals("X")) {
                result[1] = "X";
            } else {
                result[0] = g1.substring(0, g1.length() - 1);
                result[1] = g1.substring(g1.length() - 1);
            }
            if (g2.equals("X")) {
                result[3] = "X";
            } else {
                result[2] = g2.substring(0, g2.length() - 1);
                result[3] = g2.substring(g2.length() - 1);
            }
        } else {
            System.err.println("cannot parse side " + side);
        }
        return result;
    }



    class GateMouseListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            Gate gate = getGateByPosition(e.getX(), e.getY());
            System.out.println("clicked gate nr " + gates.indexOf(gate));
//            System.out.format("%s%n", getQuadrantByPosition(gate, e.getX(), e.getY()));

            // move mode
            if (frame.moveToggleButton.isSelected()) {
                if (lastSelectedGate == null) {
                    lastSelectedGate = gate;
                    System.out.println("start moving..");
                    return;
                } else {
                    System.out.println("end moving.");
                    lastSelectedGate.rect.x = e.getX();
                    lastSelectedGate.rect.y = e.getY();
                    repaint();
                    lastSelectedGate = null;
                    return;
                }
            }

            // input definition mode
            if ((inputLineDefinitionMode || outputLineDefinitionMode) && gate != null) {
                Quadrant quad = getQuadrantByPosition(gate, e.getX(), e.getY());
                int gateNr = gates.indexOf(gate);

                int line; // in or out
                if (inputLineDefinitionMode) {
                    line = INPUT_LINE;
                    inputLineGoesToGateNr = gateNr;

                } else {
                    line = OUTPUT_LINE;
                    outputLineGoesToGateNr = gateNr;
                }
                char lineC = (inputLineDefinitionMode) ? 'I' : 'O';
                switch (quad) {
                    case TOPRIGHT:
                        gate.outL = line;
                        gate.outLc = lineC;
                        break;
                    case BOTTOMRIGHT:
                        gate.outR = line;
                        gate.outRc = lineC;
                        break;
                    case TOPLEFT:
                        gate.inL = line;
                        gate.inLc = lineC;
                        break;
                    case BOTTOMLEFT:
                        gate.inR = line;
                        gate.inRc = lineC;
                        break;
                }
                inputLineDefinitionMode = false;
                outputLineDefinitionMode = false;
                repaint(300);
                return;
            }

            // connection mode
            if (gate != null) {
                if (lastSelectedGate == null) { // first gate
                    lastSelectedGate = gate;
                    lastSelectedQuadrant = getQuadrantByPosition(gate, e.getX(), e.getY());
                } else { // second gate
                    Gate gate1 = lastSelectedGate;
                    Quadrant quad1 = lastSelectedQuadrant;
                    int nrOfGate1 = gates.indexOf(gate1);
                    Gate gate2 = gate;
                    Quadrant quad2 = getQuadrantByPosition(gate2, e.getX(), e.getY());
                    int nrOfGate2 = gates.indexOf(gate2);
                    assert quad1 != null && quad2 != null;

                    switch (quad1) {
                        case TOPRIGHT:
                            gate1.outL = nrOfGate2;
                            gate1.outLc = ((quad2 == Quadrant.TOPLEFT) ? 'L' : 'R');
                            break;
                        case BOTTOMRIGHT:
                            gate1.outR = nrOfGate2;
                            gate1.outRc = ((quad2 == Quadrant.TOPLEFT) ? 'L' : 'R');
                            break;
                        case TOPLEFT:
                            gate1.inL = nrOfGate2;
                            gate1.inLc = ((quad2 == Quadrant.TOPRIGHT) ? 'L' : 'R');
                            break;
                        case BOTTOMLEFT:
                            gate1.inR = nrOfGate2;
                            gate1.inRc = ((quad2 == Quadrant.TOPRIGHT) ? 'L' : 'R');
                            break;
                    }
                    switch (quad2) {
                        case TOPRIGHT:
                            gate2.outL = nrOfGate1;
                            gate2.outLc = ((quad1 == Quadrant.TOPLEFT) ? 'L' : 'R');
                            break;
                        case BOTTOMRIGHT:
                            gate2.outR = nrOfGate1;
                            gate2.outRc = ((quad1 == Quadrant.TOPLEFT) ? 'L' : 'R');
                            break;
                        case TOPLEFT:
                            gate2.inL = nrOfGate1;
                            gate2.inLc = ((quad1 == Quadrant.TOPRIGHT) ? 'L' : 'R');
                            break;
                        case BOTTOMLEFT:
                            gate2.inR = nrOfGate1;
                            gate2.inRc = ((quad1 == Quadrant.TOPRIGHT) ? 'L' : 'R');
                            break;
                    }
                    lastSelectedGate = null;
                    lastSelectedQuadrant = null;
                    repaint(300);
                }
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}
