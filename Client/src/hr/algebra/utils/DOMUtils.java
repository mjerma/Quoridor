/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utils;

import hr.algebra.model.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static hr.algebra.controller.GameScreenController.HEIGHT;
import static hr.algebra.controller.GameScreenController.WIDTH;

public class DOMUtils {

    private static final String FILENAME_STATES = "states.xml";

    public static void saveStates(ArrayList<BoardState> states) {
        try {
            Document document = createDocument("states");
            states.forEach(s -> document.getDocumentElement().appendChild(createStateElement(s, document)));
            saveDocument(document, FILENAME_STATES);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static Element createStateElement(BoardState s, Document document) throws DOMException {
        Element stateElement = document.createElement("state");
        Element tilesElement = document.createElement("tiles");
        Element verticalWallsElement = document.createElement("verticalWalls");
        Element horizontalWallsElement = document.createElement("horizontalWalls");
        Element playersElement = document.createElement("players");
        Element currentPlayerElement = document.createElement("currentPlayer");
        Element timeElapsedElement = document.createElement("timeElapsed");

        stateElement.appendChild(tilesElement);
        stateElement.appendChild(verticalWallsElement);
        stateElement.appendChild(horizontalWallsElement);
        stateElement.appendChild(currentPlayerElement);
        stateElement.appendChild(playersElement);
        stateElement.appendChild(timeElapsedElement);

        Tile[][] tiles = s.getTiles();
        Wall[][] verticalWalls = s.getVerticalWalls();
        Wall[][] horizontalWalls = s.getHorizontalWalls();
        ArrayList<Player> players = s.getPlayers();
        Player currentPlayer = s.getCurrentPlayer();
        int timeElapsed = s.getTimeElapsed();

        for (int y = 0; y < HEIGHT + 1; y++) {
            for (int x = 0; x < WIDTH + 1; x++) {
                if (y < HEIGHT && x < WIDTH) {
                    tilesElement.appendChild(createTileElement(tiles[x][y], document));
                }
                if (y < HEIGHT) {
                    verticalWallsElement.appendChild(createWallElement(verticalWalls[x][y], document));
                }
                if (x < WIDTH) {
                    horizontalWallsElement.appendChild(createWallElement(horizontalWalls[x][y], document));
                }
            }
        }

        players.forEach(p -> playersElement.appendChild(createPlayerElement(p, "player", document)));
        currentPlayerElement.appendChild(createPlayerElement(currentPlayer, "currentPlayer", document));
        timeElapsedElement.setTextContent(Integer.toString(timeElapsed));

        return stateElement;
    }

    private static Element createTileElement(Tile t, Document document) throws DOMException {
        Element tileElement = document.createElement("tile");
        tileElement.appendChild(createElement(document, "xCoordinate", Integer.toString(t.getXCoordinate())));
        tileElement.appendChild(createElement(document, "yCoordinate", Integer.toString(t.getYCoordinate())));
        tileElement.appendChild(createPlayerElement(t.getPlayer(), "player", document));
        return tileElement;
    }

    private static Element createWallElement(Wall w, Document document) throws DOMException {
        Element wallElement = document.createElement("wall");
        wallElement.appendChild(createElement(document, "xCoordinate", Integer.toString(w.getXCoordinate())));
        wallElement.appendChild(createElement(document, "yCoordinate", Integer.toString(w.getYCoordinate())));
        wallElement.appendChild(createElement(document, "isVertical", Boolean.toString(w.isVertical())));
        wallElement.appendChild(createPlayerElement(w.getPlayer(), "placedBy", document));
        return wallElement;
    }

    private static Element createPlayerElement(Player p, String tagName, Document document) throws DOMException {
        Element playerElement = document.createElement(tagName);
        if (p != null) {
            playerElement.appendChild(createElement(document, "color", PlayerColor.getName(p.getColor())));
            playerElement.appendChild(createElement(document, "currentX", Double.toString(p.getCurrentX())));
            playerElement.appendChild(createElement(document, "currentY", Double.toString(p.getCurrentY())));
            playerElement.appendChild(createElement(document, "wallCount", Integer.toString(p.getWallCount())));
        }
        return playerElement;
    }

    private static Document createDocument(String element) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation domImplementation = builder.getDOMImplementation();
        DocumentType documentType = domImplementation.createDocumentType("DOCTYPE", null, "employees.dtd");
        return domImplementation.createDocument(null, element, documentType);
    }

    private static Node createElement(Document document, String tagName, String data) {
        Element element = document.createElement(tagName);
        Text text = document.createTextNode(data);
        element.appendChild(text);
        return element;
    }

    private static void saveDocument(Document document, String fileName) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(document), new StreamResult(new File(fileName)));
    }

    public static ArrayList<BoardState> loadStates() {
        ArrayList<BoardState> states = new ArrayList<>();
        try {
            Document document = createDocument(new File(FILENAME_STATES));
            NodeList nodes = document.getElementsByTagName("state");
            for (int i = 0; i < nodes.getLength(); i++) {
                states.add(processStateNode((Element) nodes.item(i)));
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return states;
    }

    private static Document createDocument(File file) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        return document;
    }

    private static BoardState processStateNode(Element state) {
        return new BoardState(
                processTilesNode((Element) state.getElementsByTagName("tiles").item(0)),
                processWallsNode((Element) state.getElementsByTagName("horizontalWalls").item(0), false),
                processWallsNode((Element) state.getElementsByTagName("verticalWalls").item(0), true),
                processPlayersNode((Element) state.getElementsByTagName("players").item(0)),
                processPlayerNode((Element) state.getElementsByTagName("currentPlayer").item(0)),
                Integer.parseInt(state.getElementsByTagName("timeElapsed").item(0).getTextContent()));
    }

    private static Tile[][] processTilesNode(Element tiles) {
        Tile[][] tileArray = new Tile[WIDTH][HEIGHT];;

        try {
            NodeList nodes = tiles.getElementsByTagName("tile");
            for (int i = 0; i < nodes.getLength(); i++) {
                Tile temp = processTileNode((Element) nodes.item(i));
                tileArray[temp.getXCoordinate()][temp.getYCoordinate()] = temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tileArray;
    }

    private static Wall[][] processWallsNode(Element walls, boolean vertical) {
        Wall[][] wallArray;

        if (vertical) {
            wallArray = new Wall[WIDTH+1][HEIGHT];
        }
        else wallArray = new Wall[WIDTH][HEIGHT+1];

        try {
            NodeList nodes = walls.getElementsByTagName("wall");
            for (int i = 0; i < nodes.getLength(); i++) {
                Wall temp = processWallNode((Element) nodes.item(i));
                wallArray[temp.getXCoordinate()][temp.getYCoordinate()] = temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wallArray;
    }

    private static ArrayList<Player> processPlayersNode(Element players) {
        ArrayList<Player> playerList = new ArrayList<>();

        try {
            NodeList nodes = players.getElementsByTagName("player");
            for (int i = 0; i < nodes.getLength(); i++) {
                playerList.add(processPlayerNode(((Element) nodes.item(i))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return playerList;
    }

    private static Tile processTileNode(Element tile) {
        return new Tile(
                Integer.parseInt(tile.getElementsByTagName("xCoordinate").item(0).getTextContent()),
                Integer.parseInt(tile.getElementsByTagName("yCoordinate").item(0).getTextContent()),
                processPlayerNode((Element) tile.getElementsByTagName("player").item(0)));
    }

    private static Wall processWallNode(Element wall) {
        return new Wall(
                Integer.parseInt(wall.getElementsByTagName("xCoordinate").item(0).getTextContent()),
                Integer.parseInt(wall.getElementsByTagName("yCoordinate").item(0).getTextContent()),
                Boolean.parseBoolean(wall.getElementsByTagName("isVertical").item(0).getTextContent()),
                processPlayerNode((Element) wall.getElementsByTagName("placedBy").item(0)));
    }

    private static Player processPlayerNode(Element player) {
        if (player.getChildNodes().getLength() > 0) {
            return new Player(
                    Double.parseDouble(player.getElementsByTagName("currentX").item(0).getTextContent()),
                    Double.parseDouble(player.getElementsByTagName("currentY").item(0).getTextContent()),
                    PlayerColor.getPlayerColor(player.getElementsByTagName("color").item(0).getTextContent()),
                    Integer.parseInt(player.getElementsByTagName("wallCount").item(0).getTextContent()));
        }
        else return null;
    }
}
