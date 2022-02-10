/*
    Skeleton Program for the AQA A Level Paper 1 Summer 2022 examination
    this code should be used in conjunction with the Preliminary Material
    written by the AQA Programmer Team
    developed in NetBeans IDE 8.1 environment
 */

package breakthrough;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Game {
    
    public static void main(String[] args) {
        Breakthrough thisGame = new Breakthrough();
        thisGame.playGame();
        Console.readLine();
    }
}

class Breakthrough {
    private static Random rNoGen = new Random();
    private CardCollection deck; 
    private CardCollection hand; 
    private CardCollection sequence;
    private CardCollection discard;
    private List<Lock> locks;
    private int score;
    private boolean gameOver;
    private Lock currentLock;
    private boolean lockSolved;

    public Breakthrough(){
        deck = new CardCollection("DECK");
        hand = new CardCollection("HAND");
        sequence = new CardCollection("SEQUENCE");
        discard = new CardCollection("DISCARD");
        score = 0;
        LoadLocks();
    }

    public void playGame() {
        String menuChoice;
        if (locks.size() > 0) {
            gameOver = false;
            currentLock = new Lock();
            setupGame();
            while (!gameOver) {
                lockSolved = false;
                while (!lockSolved && !gameOver) {
                    Console.writeLine();
                    Console.writeLine("Current score: " + score);
                    Console.writeLine(currentLock.getLockDetails());
                    Console.writeLine(sequence.getCardDisplay());
                    Console.writeLine(hand.getCardDisplay());
                    menuChoice = getChoice();
                    switch (menuChoice) {
                        case "D":
                            Console.writeLine(discard.getCardDisplay());
                            break;
                        case "U":
                            int cardChoice = getCardChoice();
                            String discardOrPlay = getDiscardOrPlayChoice();
                            if (discardOrPlay.equals("D")) {
                                moveCard(hand, discard, hand.getCardNumberAt(cardChoice - 1));
                                getCardFromDeck(cardChoice);
                            } else if (discardOrPlay.equals("P")) {
                                playCardToSequence(cardChoice);
                            }
                            break;
                    }
                    if (currentLock.getLockSolved()) {
                        lockSolved = true;
                        processLockSolved();
                    }
                }
                gameOver = checkIfPlayerHasLost();
            }
        } else {
            Console.writeLine("No locks in file.");
        }
    }

    private void processLockSolved() {
        score += 10;
        Console.writeLine("Lock has been solved.  Your score is now: " + score);
        while (discard.getNumberOfCards() > 0) {
            moveCard(discard, deck, discard.getCardNumberAt(0));
        }
        deck.shuffle();
        currentLock = getRandomLock();
    }

    private boolean checkIfPlayerHasLost() {
        if (deck.getNumberOfCards() == 0) {
            Console.writeLine("You have run out of cards in your deck.  Your final score is: " + score);
            return true;
        } else {
            return false;
        }
    }

    private void setupGame() {
        String choice;
        Console.write("Enter L to load a game from a file, anything else to play a new game:> ");
        choice = Console.readLine().toUpperCase();
        if (choice.equals("L")) {
            if (!loadGame("game1.txt")) {
                gameOver = true;
            }
        } else {
            createStandardDeck();
            deck.shuffle();
            for (int count = 1; count <= 5; count++) {
                moveCard(deck, hand, deck.getCardNumberAt(0));
            }
            addDifficultyCardsToDeck();
            deck.shuffle();
            currentLock = getRandomLock();
        }
    }

    private void playCardToSequence(int cardChoice) {
        if (sequence.getNumberOfCards() > 0) {
            if (hand.getCardDescriptionAt(cardChoice - 1).charAt(0) != sequence.getCardDescriptionAt(sequence.getNumberOfCards() - 1).charAt(0)) {
                score += moveCard(hand, sequence, hand.getCardNumberAt(cardChoice - 1));
                getCardFromDeck(cardChoice);
            }
        } else {
            score += moveCard(hand, sequence, hand.getCardNumberAt(cardChoice - 1));
            getCardFromDeck(cardChoice);
        }
        if (checkIfLockChallengeMet()) {
            Console.writeLine();
            Console.writeLine("A challenge on the lock has been met.");
            Console.writeLine();
            score += 5;
        }
    }

    private boolean checkIfLockChallengeMet() {
        String sequenceAsString = "";
        for (int count = sequence.getNumberOfCards() - 1; count >= Math.max(0, sequence.getNumberOfCards() - 3); count--) {
            if (sequenceAsString.length() > 0) {
                sequenceAsString = ", " + sequenceAsString;
            }
            sequenceAsString = sequence.getCardDescriptionAt(count) + sequenceAsString;
            if (currentLock.checkIfConditionMet(sequenceAsString)) {
                return true;
            }
        }
        return false;
    }

    private void setupCardCollectionFromGameFile(String lineFromFile, CardCollection cardCol) {
        List<String> splitLine;
        int cardNumber;
        if (lineFromFile.length() > 0) {
            splitLine = Arrays.asList(lineFromFile.split(","));
            for (String item : splitLine) {
                if (item.length() == 5) {
                    cardNumber = Integer.parseInt(item.charAt(4) + "");
                } else {
                    cardNumber = Integer.parseInt(item.substring(4, 6));
                }
                if (item.substring(0, 3).equals("Dif")) {
                    DifficultyCard currentCard = new DifficultyCard(cardNumber);
                    cardCol.addCard(currentCard);
                } else {
                    ToolCard currentCard = new ToolCard(item.charAt(0) + "", item.charAt(2) + "", cardNumber);
                    cardCol.addCard(currentCard);
                }
            }
        }
    }

    private void setupLock(String line1, String line2) {
        List<String> splitLine;
        splitLine = Arrays.asList(line1.split(";"));
        for (String item : splitLine) {
            List<String> conditions;
            conditions = Arrays.asList(item.split(","));
            currentLock.addChallenge(conditions);
        }
        splitLine = Arrays.asList(line2.split(";"));
        for (int count = 0; count < splitLine.size(); count++) {
            if (splitLine.get(count).equals("Y")) {
                currentLock.setChallengeMet(count, true);
            }
        }
    }

    private boolean loadGame(String fileName) {
        String lineFromFile;
        String lineFromFile2;
        try {
            BufferedReader myStream = new BufferedReader(new FileReader(fileName));
            lineFromFile = myStream.readLine();
            score = Integer.parseInt(lineFromFile);
            lineFromFile = myStream.readLine();
            lineFromFile2 = myStream.readLine();
            setupLock(lineFromFile, lineFromFile2);
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, hand);
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, sequence);
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, discard);
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, deck);
            myStream.close();
            return true;
        } catch (Exception e) {
            Console.writeLine("File not loaded");
            return false;
        }
    }

    private void LoadLocks() {
        String fileName = "locks.txt";
        String lineFromFile;
        List<String> challenges;
        locks = new ArrayList<>();
        try {
            BufferedReader myStream = new BufferedReader(new FileReader(fileName));
                lineFromFile = myStream.readLine();
                while (lineFromFile != null) {
                    challenges = Arrays.asList(lineFromFile.split(";"));
                    Lock lockFromFile = new Lock();
                    for (String c : challenges) {
                        List<String> conditions;
                        conditions = Arrays.asList(c.split(","));
                        lockFromFile.addChallenge(conditions);
                    }
                    locks.add(lockFromFile);
                    lineFromFile = myStream.readLine();
                }
        } catch (Exception e) {
            Console.writeLine("File not loaded");
        }
    }

    private Lock getRandomLock() {
        return locks.get(rNoGen.nextInt(locks.size()));
    }

    private void getCardFromDeck(int cardChoice) {
        if (deck.getNumberOfCards() > 0) {
            if (deck.getCardDescriptionAt(0).equals("Dif")) {
                Card currentCard = deck.removeCard(deck.getCardNumberAt(0));
                Console.writeLine();
                Console.writeLine("Difficulty encountered!");
                Console.writeLine(hand.getCardDisplay());
                Console.write("To deal with this you need to either lose a key ");
                Console.write("(enter 1-5 to specify position of key) or (D)iscard five cards from the deck:> ");
                String choice = Console.readLine();
                Console.writeLine();
                discard.addCard(currentCard);
                currentCard.process(deck, discard, hand, sequence, currentLock, choice, cardChoice);
            }
        }
        while (hand.getNumberOfCards() < 5 && deck.getNumberOfCards() > 0) {
            if (deck.getCardDescriptionAt(0).equals("Dif")) {
                moveCard(deck, discard, deck.getCardNumberAt(0));
                Console.writeLine("A difficulty card was discarded from the deck when refilling the hand.");
            } else {
                moveCard(deck, hand, deck.getCardNumberAt(0));
            }
        }
        if (deck.getNumberOfCards() == 0 && hand.getNumberOfCards() < 5) {
            gameOver = true;
        }
    }

    private int getCardChoice() {
        String choice;
        int value = 0;
        boolean parsed;
        do {
            Console.write("Enter a number between 1 and 5 to specify card to use:> ");
            choice = Console.readLine();
            try {
                value = Integer.parseInt(choice);
                parsed = true;
            } catch (NumberFormatException e) {
                parsed = false;
            }
        } while (!parsed);
        return value;
    }

    private String getDiscardOrPlayChoice() {
        String choice;
        Console.write("(D)iscard or (P)lay?:> ");
        choice = Console.readLine().toUpperCase();
        return choice;
    }

    private String getChoice() {
        Console.writeLine();
        Console.write("(D)iscard inspect, (U)se card:> ");
        String choice = Console.readLine().toUpperCase();
        return choice;
    }

    private void addDifficultyCardsToDeck() {
        for (int count = 1; count <= 5; count++) {
            deck.addCard(new DifficultyCard());
        }
    }

    private void createStandardDeck() {
        Card newCard;
        for (int count = 1; count <= 5; count++) {
            newCard = new ToolCard("P", "a");
            deck.addCard(newCard);
            newCard = new ToolCard("P", "b");
            deck.addCard(newCard);
            newCard = new ToolCard("P", "c");
            deck.addCard(newCard);
        }
        for (int count = 1; count <= 3; count++) {
            newCard = new ToolCard("F", "a");
            deck.addCard(newCard);
            newCard = new ToolCard("F", "b");
            deck.addCard(newCard);
            newCard = new ToolCard("F", "c");
            deck.addCard(newCard);
            newCard = new ToolCard("K", "a");
            deck.addCard(newCard);
            newCard = new ToolCard("K", "b");
            deck.addCard(newCard);
            newCard = new ToolCard("K", "c");
            deck.addCard(newCard);
        }
    }

    private int moveCard(CardCollection fromCollection, CardCollection toCollection, int cardNumber) {
        int score = 0;
        if (fromCollection.getName().equals("HAND") && toCollection.getName().equals("SEQUENCE")) {
            Card cardToMove = fromCollection.removeCard(cardNumber);
            if (cardToMove != null) {
                toCollection.addCard(cardToMove);
                score = cardToMove.getScore();
            }
        } else {
            Card cardToMove = fromCollection.removeCard(cardNumber);
            if (cardToMove != null) {
                toCollection.addCard(cardToMove);
            }
        }
        return score;
    }
}

class Challenge {
    protected List<String> condition;
    protected boolean met;

    public Challenge() {
        met = false;
    }
    public boolean getMet() {
        return met;
    }

    public List<String> getCondition() {
        return condition;
    }

    public void SetMet(boolean newValue) {
        met = newValue;
    }

    public void setCondition(List<String> newCondition) {
        condition = newCondition;
    }
}

class Lock {
    protected List<Challenge> challenges = new ArrayList<>();

    public void addChallenge(List<String> condition) {
        Challenge c = new Challenge();
        c.setCondition(condition);
        challenges.add(c);
    }

    private String convertConditionToString(List<String> c) {
        String conditionAsString = "";
        for (int pos = 0; pos <= c.size() - 2; pos++) {
            conditionAsString += c.get(pos) + ", ";
        }
        conditionAsString += c.get(c.size() - 1);
        return conditionAsString;
    }

    public String getLockDetails() {
        String lockDetails = System.lineSeparator() + "CURRENT LOCK" + System.lineSeparator() + "------------" + System.lineSeparator();
        for (Challenge c : challenges) {
            if (c.getMet()) {
                lockDetails += "Challenge met: ";
            } else {
                lockDetails += "Not met:       ";
            }
            lockDetails += convertConditionToString(c.getCondition()) + System.lineSeparator();
        }
        lockDetails += System.lineSeparator();
        return lockDetails;
    }

    public boolean getLockSolved() {
        for (Challenge c : challenges) {
            if (!c.getMet()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIfConditionMet(String sequence) {
        for (Challenge c : challenges) {
            if (!c.getMet() && sequence.equals(convertConditionToString(c.getCondition()))) {
                c.SetMet(true);
                return true;
            }
        }
        return false;
    }

    public void setChallengeMet(int pos, boolean value) {
        challenges.get(pos).SetMet(value);
    }

    public boolean getChallengeMet(int pos) {
        return challenges.get(pos).getMet();
    }

    public int getNumberOfChallenges() {
        return challenges.size();
    }
}

class Card {
    protected int cardNumber, score;
    protected static int nextCardNumber = 1;

    public Card() {
        cardNumber = nextCardNumber;
        nextCardNumber += 1;
        score = 0;
    }

    public int getScore() {
        return score;
    }

    public void process(CardCollection deck, CardCollection discard, CardCollection hand, CardCollection sequence, Lock currentLock, String choice, int cardChoice) {
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public String  getDescription() {
        if (cardNumber < 10) {
            return " " + cardNumber;
        } else {
            return cardNumber + "";
        }
    }
}

class ToolCard extends Card {
    protected String toolType;
    protected String kit;

    public ToolCard(String t, String k) {
        super();
        toolType = t;
        kit = k;
        setScore();
    }

    public ToolCard(String t, String k, int cardNo) {
        toolType = t;
        kit = k;
        cardNumber = cardNo;
        setScore();
    }

    private void setScore() {
        switch (toolType) {
            case "K":
                score = 3;
                break;
            case "F":
                score = 2;
                break;
            case "P":
                score = 1;
                break;
        }
    }

    @Override
    public String getDescription() {
        return toolType + " " + kit;
    }
}

class DifficultyCard extends Card {
    protected String cardType;

    public DifficultyCard() {
        super();
        cardType = "Dif";
    }

    public DifficultyCard(int cardNo) {
        cardType = "Dif";
        cardNumber = cardNo;
    }
    
    @Override
    public String getDescription() {
        return cardType;
    }

    @Override
    public void process(CardCollection deck, CardCollection discard, CardCollection hand, CardCollection sequence, Lock currentLock, String choice, int cardChoice) {
        int choiceAsInteger = 0;
        boolean parsed;
        try {
            choiceAsInteger = Integer.parseInt(choice);
            parsed = true;
        } catch (NumberFormatException e) {
            parsed = false;
        } 
        if (parsed) {
            if (choiceAsInteger >= 1 && choiceAsInteger <= 5) {
                if (choiceAsInteger >= cardChoice) {
                    choiceAsInteger -= 1;
                }
                if (choiceAsInteger > 0) {
                    choiceAsInteger -= 1;
                }
                if (hand.getCardDescriptionAt(choiceAsInteger).charAt(0) == 'K') {
                    Card cardToMove = hand.removeCard(hand.getCardNumberAt(choiceAsInteger));
                    discard.addCard(cardToMove);
                    return;
                }
            }
        }
        int count = 0;
        while (count < 5 && deck.getNumberOfCards() > 0) {
            Card cardToMove = deck.removeCard(deck.getCardNumberAt(0));
            discard.addCard(cardToMove);
            count += 1;
        }
    }
}

class CardCollection {
    protected List<Card> cards = new ArrayList<>();
    protected String name;

    public CardCollection(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public int getCardNumberAt(int x) {
        return cards.get(x).getCardNumber();
    }

    public String getCardDescriptionAt(int x) {
        return cards.get(x).getDescription();
    }

    public void addCard(Card c) {
        cards.add(c);
    }

    public int getNumberOfCards() {
        return cards.size();
    }

    public void shuffle() {
        Card tempCard;
        int rNo1, rNo2;
        Random rNoGen = new Random();
        for (int count = 1; count <= 10000; count++) {
            rNo1 = rNoGen.nextInt(cards.size());
            rNo2 = rNoGen.nextInt(cards.size());
            tempCard = cards.get(rNo1);
            cards.set(rNo1, cards.get(rNo2));
            cards.set(rNo2, tempCard);
        }
    }

    public Card removeCard(int cardNumber) {
        boolean cardFound = false;
        int pos = 0;
        Card cardToGet = null;
        while (pos < cards.size() && !cardFound) {
            if (cards.get(pos).getCardNumber() == cardNumber) {
                cardToGet = cards.get(pos);
                cardFound = true;
                cards.remove(pos);
            }
            pos++;
        }
        return cardToGet;
    }

    private String createLineOfDashes(int size) {
        String lineOfDashes = "";
        for (int count = 1; count <= size; count++) {
            lineOfDashes += "------";
        }
        return lineOfDashes;
    }

    public String getCardDisplay() {
        String cardDisplay = System.lineSeparator() + name + ":";
        if (cards.isEmpty()) {
            return cardDisplay + " empty" + System.lineSeparator() + System.lineSeparator();
        } else {
            cardDisplay += System.lineSeparator() + System.lineSeparator();
        }
        String lineOfDashes;
        final int cardsPerLine = 10;
        if (cards.size() > cardsPerLine) {
            lineOfDashes = createLineOfDashes(cardsPerLine);
        } else {
            lineOfDashes = createLineOfDashes(cards.size());
        }
        cardDisplay += lineOfDashes + System.lineSeparator();
        boolean complete = false;
        int pos = 0;
        while (!complete) {
            cardDisplay += "| " + cards.get(pos).getDescription() + " ";
            pos += 1;
            if (pos % cardsPerLine == 0) {
                cardDisplay += "|" + System.lineSeparator() + lineOfDashes + System.lineSeparator();
            }
            if (pos == cards.size()) {
                complete = true;
            }
        }
        if (cards.size() % cardsPerLine > 0) {
            cardDisplay += "|" + System.lineSeparator();
            if (cards.size() > cardsPerLine) {
                lineOfDashes = createLineOfDashes(cards.size() % cardsPerLine);
            }
            cardDisplay += lineOfDashes + System.lineSeparator();
        }
        return cardDisplay;
    }
}