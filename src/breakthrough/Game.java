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

    /**
     * Sets up the game by initialising the deck, hand, sequence and discard
     */
    public Breakthrough() {
        // Creates empty card collection with a set name/identifier
        deck = new CardCollection("DECK");
        hand = new CardCollection("HAND");
        sequence = new CardCollection("SEQUENCE");
        discard = new CardCollection("DISCARD");

        // Sets score to zero and sets up the locks for the game
        score = 0;
        LoadLocks();
    }

    /**
     * Main Procedure which controls the flow of the game
     */
    public void playGame() {
        String menuChoice;
        // While there are still locks to be solved
        if (locks.size() > 0) {
            // Init
            gameOver = false;
            currentLock = new Lock();
            setupGame();
            // While the game is not over
            while (!gameOver) {
                lockSolved = false;
                // While the lock has not been solved and the player has not lost
                while (!lockSolved && !gameOver) {
                    // The game display
                    Console.writeLine();
                    Console.writeLine("Current score: " + score);
                    Console.writeLine(currentLock.getLockDetails());
                    Console.writeLine(sequence.getCardDisplay());
                    Console.writeLine(hand.getCardDisplay());
                    // Asks if the player wants to discard inspect or use a card
                    menuChoice = getChoice();
                    switch (menuChoice) {
                        case "D":
                            // Shows the discard pile
                            Console.writeLine(discard.getCardDisplay());
                            break;
                        case "U":
                            // Asks the user which card they want to use
                            int cardChoice = getCardChoice();
                            // Asks the user whether they want to discard or use the card
                            String discardOrPlay = getDiscardOrPlayChoice();
                            if (discardOrPlay.equals("D")) {
                                // Discards the selected card
                                moveCard(hand, discard, hand.getCardNumberAt(cardChoice - 1));
                                // Fetches a card from the deck to add to the user's hand
                                getCardFromDeck(cardChoice);
                            } else if (discardOrPlay.equals("P")) {
                                // Plays the selected card to the sequence
                                playCardToSequence(cardChoice);
                            }
                            break;
                    }
                    // Checks if the lock has been solved i.e. if any of the challenges have been solved
                    if (currentLock.getLockSolved()) {
                        lockSolved = true;
                        processLockSolved();
                    }
                }
                // Checks if the player has lost
                gameOver = checkIfPlayerHasLost();
            }
        } else {
            // Throws error if there are no locks in the locks file
            Console.writeLine("No locks in file.");
        }
    }

    /**
     * Executed when the player has solved a lock
     * <p>
     * Adds 10 to the user's score
     * <br>
     * Moves all cards from the discard pile to the deck
     * <br>
     * Shuffles the deck
     * <br>
     * Gets a new lock
     */
    private void processLockSolved() {
        score += 10;
        Console.writeLine("Lock has been solved.  Your score is now: " + score);
        // Moves all cards from the discard pile to the deck
        while (discard.getNumberOfCards() > 0) {
            moveCard(discard, deck, discard.getCardNumberAt(0));
        }
        deck.shuffle();
        currentLock = getRandomLock();
    }

    /**
     * If the deck has no cards left and the player's hand can't be filled with 5 card, the player has lost
     * <br>
     * Prints out the score
     *
     * @return true if the player has lost, false otherwise
     */
    private boolean checkIfPlayerHasLost() {
        if (deck.getNumberOfCards() == 0) {
            Console.writeLine("You have run out of cards in your deck.  Your final score is: " + score);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Offers the user an option to load a game (from game1.txt) or play a new game
     * <p>
     * Creates a standard 33-card deck
     * <br>
     * Shuffles the deck
     * <p>
     * Takes the first 5 cards from the deck and moves them to the player's hand
     * <p>
     * 5 Difficulty cards are added to the deck
     * <br>
     * The deck is reshuffled
     * <p>
     * A random lock is chosen to be solved
     */
    private void setupGame() {
        String choice;
        Console.write("Enter L to load a game from a file, anything else to play a new game:> ");
        choice = Console.readLine().toUpperCase();
        if (choice.equals("L")) {
            if (!loadGame("game1.txt")) {
                // If the game can't be loaded, the game ends
                gameOver = true;
            }
        } else {
            // Creates a standard 33-card deck, shuffles it,
            // Takes the first 5 cards from the deck and moves them to the player's hand
            createStandardDeck();
            deck.shuffle();
            for (int count = 1; count <= 5; count++) {
                moveCard(deck, hand, deck.getCardNumberAt(0));
            }
            // 5 Difficulty cards are added to the deck,
            // The deck is reshuffled,
            // A random lock is chosen to be solved
            addDifficultyCardsToDeck();
            deck.shuffle();
            currentLock = getRandomLock();
        }
    }

    /**
     * Plays the selected card to the sequence
     * <br>
     * Checks if the card is valid i.e. can't play the same type of tool twice in a row
     * <br>
     * After the card is played to the sequence, it checks if the card has solved any challenges
     * @param cardChoice The card the user has selected to play to the sequence
     */
    private void playCardToSequence(int cardChoice) {
        if (sequence.getNumberOfCards() > 0) {
            // If the user is trying to play a card different to the last card in the sequence
            // This is checked to ensure the same type of tool is not played twice in a row
            if (hand.getCardDescriptionAt(cardChoice - 1).charAt(0) != sequence.getCardDescriptionAt(sequence.getNumberOfCards() - 1).charAt(0)) {
                // Moves the card from the user's hand to the sequence and fetches a new card from the deck
                score += moveCard(hand, sequence, hand.getCardNumberAt(cardChoice - 1));
                getCardFromDeck(cardChoice);
            }
        } else {
            // Since the sequence is empty, the user can play any card
            score += moveCard(hand, sequence, hand.getCardNumberAt(cardChoice - 1));
            getCardFromDeck(cardChoice);
        }

        // Checks if the user has solved a challenge in the lock
        if (checkIfLockChallengeMet()) {
            Console.writeLine();
            Console.writeLine("A challenge on the lock has been met.");
            Console.writeLine();
            score += 5;
        }
    }

    /**
     * Checks if any of the challenges have been met by the new card added to the sequence
     * <p>
     * Note: Returns true immediately after the first challenge is met meaning there could be other challenges that were met
     * @return true if a challenge has been met, false otherwise
     */
    private boolean checkIfLockChallengeMet() {
        String sequenceAsString = "";
        // Loops through the sequence from the last card to the first card
        // Adds the card to the sequence and then checks if it meets any of the challenges
        for (int count = sequence.getNumberOfCards() - 1; count >= Math.max(0, sequence.getNumberOfCards() - 3); count--) {
            // Only adds commas if the sequence contains at least 1 card
            if (sequenceAsString.length() > 0) {
                sequenceAsString = ", " + sequenceAsString;
            }

            // Adds the next card to the sequence
            sequenceAsString = sequence.getCardDescriptionAt(count) + sequenceAsString;
            // Checks if the sequence meets any of the challenges
            if (currentLock.checkIfConditionMet(sequenceAsString)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts card data from a string and adds it to the specified {@link CardCollection}
     *
     * @param lineFromFile The string containing the card data
     * @param cardCol      the {@link CardCollection} to add the cards to
     */
    private void setupCardCollectionFromGameFile(String lineFromFile, CardCollection cardCol) {
        List<String> splitLine;
        int cardNumber;
        if (lineFromFile.length() > 0) {
            splitLine = Arrays.asList(lineFromFile.split(","));
            for (String item : splitLine) {
                // If the item length is 5 then the card number is a single digit, two digits otherwise
                if (item.length() == 5) {
                    cardNumber = Integer.parseInt(item.charAt(4) + "");
                } else {
                    cardNumber = Integer.parseInt(item.substring(4, 6));
                }
                // If the item starts with 'Dif' then the card is a difficulty card, otherwise it is a normal card (toolcard)
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

    /**
     * Loads a lock from the game file
     *
     * @param line1 The challenges in the lock
     * @param line2 The lock status - challenges met or not
     */
    private void setupLock(String line1, String line2) {
        List<String> splitLine;
        splitLine = Arrays.asList(line1.split(";"));
        // Adds all the challenges to the lock
        for (String item : splitLine) {
            List<String> conditions;
            conditions = Arrays.asList(item.split(","));
            currentLock.addChallenge(conditions);
        }
        // Changes each lock to either met or unmet
        splitLine = Arrays.asList(line2.split(";"));
        for (int count = 0; count < splitLine.size(); count++) {
            if (splitLine.get(count).equals("Y")) {
                currentLock.setChallengeMet(count, true);
            }
        }
    }

    /**
     * Loads the game from a file
     *
     * @param fileName the name of the file to load
     * @return true if the game was loaded successfully, false otherwise
     */
    private boolean loadGame(String fileName) {
        String lineFromFile;
        String lineFromFile2;
        try {
            BufferedReader myStream = new BufferedReader(new FileReader(fileName));

            // 1st line in game file = score
            lineFromFile = myStream.readLine();
            score = Integer.parseInt(lineFromFile);

            // 2nd and 3rd line in game file = Lock + Lock status
            lineFromFile = myStream.readLine();
            lineFromFile2 = myStream.readLine();
            setupLock(lineFromFile, lineFromFile2);

            // 4th line in game file = hand
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, hand);

            // 5th line in game file = sequence
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, sequence);

            // 6th line in game file = discard
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, discard);

            // 7th line in game file = deck
            lineFromFile = myStream.readLine();
            setupCardCollectionFromGameFile(lineFromFile, deck);

            // Closes the buffered reader and returns true to indicate success
            myStream.close();
            return true;

        } catch (Exception e) {
            // Returns false to indicate failure
            Console.writeLine("File not loaded");
            return false;
        }
    }

    /**
     * Loads all the locks (and their challenges) into the program
     * <p>
     * All the loaded locks are stored in the global {@link Breakthrough#locks} list
     */
    private void LoadLocks() {
        // Init
        String fileName = "locks.txt";
        String lineFromFile;
        List<String> challenges;
        locks = new ArrayList<>();
        try {
            // Sets up the buffered reader
            BufferedReader myStream = new BufferedReader(new FileReader(fileName));
            lineFromFile = myStream.readLine();
            while (lineFromFile != null) {
                // Splits each line in the file (lock) into an array of strings (challenges)
                challenges = Arrays.asList(lineFromFile.split(";"));
                Lock lockFromFile = new Lock();
                for (String c : challenges) {
                    List<String> conditions;
                    // Splits each challenge into its conditions
                    conditions = Arrays.asList(c.split(","));
                    lockFromFile.addChallenge(conditions);
                }
                // Adds the lock to the global list of locks
                locks.add(lockFromFile);
                lineFromFile = myStream.readLine();
            }
        } catch (Exception e) {
            Console.writeLine("File not loaded");
        }
    }

    /**
     * Returns a random lock from the {@link Breakthrough#locks} collection
     *
     * @return a random lock from the {@link Breakthrough#locks} collection
     */
    private Lock getRandomLock() {
        return locks.get(rNoGen.nextInt(locks.size()));
    }

    /**
     * Refills the player's {@link Breakthrough#hand} with the top cards from the {@link Breakthrough#deck}
     *
     * @param cardChoice the card the player wishes to play to the sequence
     */
    private void getCardFromDeck(int cardChoice) {
        // If there are cards in the deck
        if (deck.getNumberOfCards() > 0) {
            // If the first card is a difficulty card then it is dealt with
            if (deck.getCardDescriptionAt(0).equals("Dif")) {
                // Prints out difficulty card dialog
                Card currentCard = deck.removeCard(deck.getCardNumberAt(0));
                Console.writeLine();
                Console.writeLine("Difficulty encountered!");
                Console.writeLine(hand.getCardDisplay());
                Console.write("To deal with this you need to either lose a key ");
                Console.write("(enter 1-5 to specify position of key) or (D)iscard five cards from the deck:> ");
                String choice = Console.readLine();
                Console.writeLine();
                // Discards the encountered difficulty card
                discard.addCard(currentCard);
                // Allows the user to choose between discarding a key from their hand or discarding five cards
                currentCard.process(deck, discard, hand, sequence, currentLock, choice, cardChoice);
            }
        }

        // Refills the player's hand with cards from the deck
        // Prints a message if a difficulty card is removed in the process
        while (hand.getNumberOfCards() < 5 && deck.getNumberOfCards() > 0) {
            if (deck.getCardDescriptionAt(0).equals("Dif")) {
                moveCard(deck, discard, deck.getCardNumberAt(0));
                Console.writeLine("A difficulty card was discarded from the deck when refilling the hand.");
            } else {
                moveCard(deck, hand, deck.getCardNumberAt(0));
            }
        }
        // If the deck has no cards left and the player does not have 5 cards in their hand then game over
        if (deck.getNumberOfCards() == 0 && hand.getNumberOfCards() < 5) {
            gameOver = true;
        }
    }

    /**
     * Returns the card which user has chosen to select (1-5)
     * <p>
     * This is the second user input prompt
     * @return the card which user has chosen to select (1-5)
     */
    private int getCardChoice() {
        String choice;
        int value = 0;
        boolean parsed;
        // Do while loop to ensure that the user enters a valid integer
        // Loops until the user enters a valid integer
        do {
            Console.write("Enter a number between 1 and 5 to specify card to use:> ");
            choice = Console.readLine();
            try {
                // Parses the input to an integer
                value = Integer.parseInt(choice);
                parsed = true;
            } catch (NumberFormatException e) {
                parsed = false;
            }
        } while (!parsed);
        // Returns the integer if it is between 1 and 5 (inclusive)
        return value;
    }

    /**
     * Prompts the user for input and returns the choice of the user to discard or play a card
     * <p>
     * This is the third user input prompt
     * @return the choice of the user (D or P)
     */
    private String getDiscardOrPlayChoice() {
        String choice;
        Console.write("(D)iscard or (P)lay?:> ");
        choice = Console.readLine().toUpperCase();
        return choice;
    }

    /**
     * Prompts the user for input and returns the choice of the player to discard or use a card
     * <p>
     * This is the first user input prompt
     * @return the choice of the player (D or U)
     */
    private String getChoice() {
        Console.writeLine();
        Console.write("(D)iscard inspect, (U)se card:> ");
        String choice = Console.readLine().toUpperCase();
        return choice;
    }

    /**
     * Adds 5 difficulty cards to the deck
     * <p>
     * This is executed after the player has their 5 initial cards in their hand
     */
    private void addDifficultyCardsToDeck() {
        for (int count = 1; count <= 5; count++) {
            deck.addCard(new DifficultyCard());
        }
    }

    /**
     * Loads all the toolkits into the deck
     * <br>
     * 3 Toolkits: Acute Kit, Basic Kit, Crude Kit
     * <br>
     * 3 Tools: Pick, File, Key
     * <br>
     * 5 Picks in each kit
     * <br>
     * 3 Files in each kit
     * <br>
     * 3 Keys in each kit
     */
    private void createStandardDeck() {
        Card newCard;
        for (int count = 1; count <= 5; count++) {
            // Picks
            newCard = new ToolCard("P", "a");
            deck.addCard(newCard);
            newCard = new ToolCard("P", "b");
            deck.addCard(newCard);
            newCard = new ToolCard("P", "c");
            deck.addCard(newCard);
        }
        for (int count = 1; count <= 3; count++) {
            // Files
            newCard = new ToolCard("F", "a");
            deck.addCard(newCard);
            newCard = new ToolCard("F", "b");
            deck.addCard(newCard);
            newCard = new ToolCard("F", "c");
            deck.addCard(newCard);

            // Keys
            newCard = new ToolCard("K", "a");
            deck.addCard(newCard);
            newCard = new ToolCard("K", "b");
            deck.addCard(newCard);
            newCard = new ToolCard("K", "c");
            deck.addCard(newCard);
        }
    }

    /**
     * Moves a card from one {@link CardCollection} to another
     *
     * @param fromCollection the CardCollection to move the card from
     * @param toCollection   the CardCollection to move the card to
     * @param cardNumber     the number of the card to move
     * @return score of a card when moving a card from hand to sequence
     */
    private int moveCard(CardCollection fromCollection, CardCollection toCollection, int cardNumber) {
        int score = 0;
        // If the card is moving from the hand to the sequence:
        // the score of the card is stored in the score variable
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

    /**
     * Constructor for a Challenge
     */
    public Challenge() {
        // Initialized to unmet
        met = false;
    }

    /**
     * Checks if the challenge has been met
     *
     * @return true if the challenge has been met, false otherwise
     */
    public boolean getMet() {
        return met;
    }

    /**
     * Returns the conditions of the challenge
     *
     * @return condition
     */
    public List<String> getCondition() {
        return condition;
    }

    /**
     * Sets the met state of the challenge
     *
     * @param newValue the new value of the met state
     */
    public void SetMet(boolean newValue) {
        met = newValue;
    }

    /**
     * Sets the conditions in the challenge
     *
     * @param newCondition the new conditions in the challenge
     */
    public void setCondition(List<String> newCondition) {
        condition = newCondition;
    }
}

class Lock {
    protected List<Challenge> challenges = new ArrayList<>();

    /**
     * Adds a challenge to the lock
     *
     * @param condition the condition of the challenge
     */
    public void addChallenge(List<String> condition) {
        Challenge c = new Challenge();
        c.setCondition(condition);
        challenges.add(c);
    }

    /**
     * Converts a challenge in the lock to a string
     * <p>
     * Example: ["K a", "P a", "F a"];
     * <br>
     * Returns: "K a, P a, F a"
     *
     * @param c the challenge to convert
     * @return the challenge as a string
     */
    private String convertConditionToString(List<String> c) {
        String conditionAsString = "";
        // iterator goes up to size - 2 so that there is no comma at the end
        for (int pos = 0; pos <= c.size() - 2; pos++) {
            conditionAsString += c.get(pos) + ", ";
        }
        // Final condition is added
        conditionAsString += c.get(c.size() - 1);
        return conditionAsString;
    }

    /**
     * Returns a string representation of the status of the lock
     *
     * @return string representation of the status of the lock
     */
    public String getLockDetails() {
        String lockDetails = System.lineSeparator() + "CURRENT LOCK" + System.lineSeparator() + "------------" + System.lineSeparator();
        for (Challenge c : challenges) {
            // Sets up first text
            if (c.getMet()) {
                lockDetails += "Challenge met: ";
            } else {
                lockDetails += "Not met:       ";
            }
            // Adds all the challenges in the lock to the output string
            lockDetails += convertConditionToString(c.getCondition()) + System.lineSeparator();
        }
        // Final separator
        lockDetails += System.lineSeparator();
        return lockDetails;
    }

    /**
     * Checks if a lock has been solved
     *
     * @return true if all challenges are met, false otherwise
     */
    public boolean getLockSolved() {
        // Checks if each challenge has been met
        for (Challenge c : challenges) {
            if (!c.getMet()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a certain sequence of cards meet a challenge
     *
     * @param sequence the sequence of cards to check
     * @return true if a challenge was met, false otherwise
     */
    public boolean checkIfConditionMet(String sequence) {
        for (Challenge c : challenges) {
            // If the condition has not been met yet, and it is in the sequence
            if (!c.getMet() && sequence.equals(convertConditionToString(c.getCondition()))) {
                c.SetMet(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a challenges state to met or unmet
     *
     * @param pos   position of the challenge
     * @param value true if the challenge is met, false otherwise
     */
    public void setChallengeMet(int pos, boolean value) {
        challenges.get(pos).SetMet(value);
    }

    /**
     * Checks if a certain challenge in the lock has been met
     *
     * @param pos the position of the challenge in the lock's list
     * @return true if the challenge is met, false otherwise
     */
    public boolean getChallengeMet(int pos) {
        return challenges.get(pos).getMet();
    }

    /**
     * Returns the number of challenges in the lock
     *
     * @return the number of challenges in the lock
     */
    public int getNumberOfChallenges() {
        return challenges.size();
    }
}

class Card {
    protected int cardNumber, score;
    protected static int nextCardNumber = 1;

    /**
     * Constructor for a card
     */
    public Card() {
        cardNumber = nextCardNumber;
        nextCardNumber += 1;
        score = 0;
    }

    /**
     * Returns the score of the card
     *
     * @return score of the card
     */
    public int getScore() {
        return score;
    }

    /**
     * Currently a blank overridable method
     *
     * @param deck
     * @param discard
     * @param hand
     * @param sequence
     * @param currentLock
     * @param choice
     * @param cardChoice
     */
    public void process(CardCollection deck, CardCollection discard, CardCollection hand, CardCollection sequence, Lock currentLock, String choice, int cardChoice) {
    }

    /**
     * Returns the card number of the card
     *
     * @return the card number of the card
     */
    public int getCardNumber() {
        return cardNumber;
    }

    /**
     * Returns the description of the card
     * Formats the card number into a 2 character string
     *
     * @return description of the card
     */
    public String getDescription() {
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

    /**
     * Constructor for ToolCard
     *
     * @param t type of tool card (P, F, K)
     * @param k kit of tool card (a, b, c)
     */
    public ToolCard(String t, String k) {
        super();
        toolType = t;
        kit = k;
        // Determines the score of the card based on its type
        setScore();
    }

    /**
     * Constructor for ToolCard
     * <br>
     * Used when loading a game from a file
     *
     * @param t      type of tool card
     * @param k      kit of tool card
     * @param cardNo card number of tool card
     */
    public ToolCard(String t, String k, int cardNo) {
        toolType = t;
        kit = k;
        cardNumber = cardNo;
        setScore();
    }

    /**
     * Determines the score of the tool card
     * <br>
     * Keys: 3
     * <br>
     * File: 2
     * <br>
     * Pick: 1
     */
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

    /**
     * Returns the description of tool card
     *
     * @return the description of tool card
     */
    @Override
    public String getDescription() {
        return toolType + " " + kit;
    }
}

class DifficultyCard extends Card {
    protected String cardType;

    /**
     * Constructor for the difficulty card
     */
    public DifficultyCard() {
        super();
        cardType = "Dif";
    }

    /**
     * Constructor for the difficulty card
     * <br>
     * Used when loading a game from a file
     *
     * @param cardNo the card number
     */
    public DifficultyCard(int cardNo) {
        cardType = "Dif";
        cardNumber = cardNo;
    }

    /**
     * Returns the description of the card (the card type)
     *
     * @return the description of the card (the card type)
     */
    @Override
    public String getDescription() {
        return cardType;
    }

    /**
     * Process what should happen when the user has encountered a difficulty card
     * Depending on the {@code choice}, the program will either remove a key or discard 5 cards from the deck
     * @param deck the  deck
     * @param discard the user's discard pile
     * @param hand the user's hand
     * @param sequence the sequence of cards
     * @param currentLock the current lock
     * @param choice the user's choice whether they want to discard a key (1-5) or if they want to discard 5 cards
     * @param cardChoice the card the user wants to play to the sequence
     */
    @Override
    public void process(CardCollection deck, CardCollection discard, CardCollection hand, CardCollection sequence, Lock currentLock, String choice, int cardChoice) {
        int choiceAsInteger = 0;
        boolean parsed;
        // Converts the choice to an integer
        try {
            choiceAsInteger = Integer.parseInt(choice);
            parsed = true;
        } catch (NumberFormatException e) {
            parsed = false;
        }
        // If the choice was a valid integer
        if (parsed) {
            // If the choice is between 1 and 5
            if (choiceAsInteger >= 1 && choiceAsInteger <= 5) {

                // Un/intentional bug discovered: https://gyazo.com/0e043bd6abb04defeb6c910af8b23c08
                // Because one of the cards is moved from the hand to the sequence, the options should no longer be 1-5
                // Instead, the options should be 1-4
                // Steps to reproduce:
                // 1. Load the game from the file
                // 2. U, 1, P
                // 3. U, 1, P
                // 4. Encounter a difficulty card
                // 5. Try to lose card #2 (K a)
                // Result: Game attempts to remove Card #1 instead but since it's not a key, it discards 5 cards

                if (choiceAsInteger >= cardChoice) {
                    choiceAsInteger -= 1;
                }
                // Removes one from the variable for zero indexing
                if (choiceAsInteger > 0) {
                    choiceAsInteger -= 1;
                }
                // If the chosen card is a key card, it is discarded and the method returns
                // If the chosen card is not a key, method proceeds and discards 5 cards from the deck
                if (hand.getCardDescriptionAt(choiceAsInteger).charAt(0) == 'K') {
                    // Moves the card from the hand to the discard pile
                    Card cardToMove = hand.removeCard(hand.getCardNumberAt(choiceAsInteger));
                    discard.addCard(cardToMove);
                    return;
                }
            }
        }
        // Discards 5 cards from the deck
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

    /**
     * Constructor for a CardCollection
     *
     * @param n the name of the collection
     */
    public CardCollection(String n) {
        name = n;
    }

    /**
     * Returns the name of the card collection
     *
     * @return the name of the card collection
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the card number of the card at the specified position.
     *
     * @param x the position of the card
     * @return the card number of the card at the specified position
     */
    public int getCardNumberAt(int x) {
        return cards.get(x).getCardNumber();
    }

    /**
     * Returns the description of the card at the given position.
     *
     * @param x the position of the card
     * @return the description of the card at the given position
     */
    public String getCardDescriptionAt(int x) {
        return cards.get(x).getDescription();
    }

    /**
     * Adds a card to the collection.
     *
     * @param c The card to be added.
     */
    public void addCard(Card c) {
        cards.add(c);
    }

    /**
     * Returns the number of cards in the collection
     *
     * @return the number of cards in the collection
     */
    public int getNumberOfCards() {
        return cards.size();
    }

    /**
     * Performs a fisher-yates shuffle on the cards in the collection.
     */
    public void shuffle() {
        // Init
        Card tempCard;
        int rNo1, rNo2;
        Random rNoGen = new Random();
        // Swap two random cards in the array 10000 times
        for (int count = 1; count <= 10000; count++) {
            // Pick two random indexes
            rNo1 = rNoGen.nextInt(cards.size());
            rNo2 = rNoGen.nextInt(cards.size());

            // Swap cards
            tempCard = cards.get(rNo1);
            cards.set(rNo1, cards.get(rNo2));
            cards.set(rNo2, tempCard);
        }
    }

    /**
     * Removes the card at the specified index.
     *
     * @param cardNumber The index of the card to remove.
     * @return the card that was removed, null if the card does not exist
     */
    public Card removeCard(int cardNumber) {
        // Init
        boolean cardFound = false;
        int pos = 0;
        Card cardToGet = null;

        // Search for the card
        while (pos < cards.size() && !cardFound) {
            // If the card is found, remove it
            if (cards.get(pos).getCardNumber() == cardNumber) {
                // Save the removed card
                cardToGet = cards.get(pos);
                cardFound = true;
                cards.remove(pos);
            }
            pos++;
        }
        return cardToGet;
    }

    /**
     * Creates a string with 6*{@code size} dashes
     *
     * @param size The number of dashes to create
     * @return A string of dashes
     */
    private String createLineOfDashes(int size) {
        String lineOfDashes = "";
        for (int count = 1; count <= size; count++) {
            lineOfDashes += "------";
        }
        return lineOfDashes;
    }

    /**
     * Creates a string representation of the cards in the collection.
     *
     * @return the string representation of the cards
     */
    public String getCardDisplay() {
        String cardDisplay = System.lineSeparator() + name + ":";
        // If the card collection is empty, return "CollectionName: empty"
        if (cards.isEmpty()) {
            return cardDisplay + " empty" + System.lineSeparator() + System.lineSeparator();
        } else {
            cardDisplay += System.lineSeparator() + System.lineSeparator();
        }

        // Creates a line of dashes
        // Maximum size is 10,
        // If there are fewer cards in the collection, then the amount of dashes is adjusted
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
            // Adds the card to the display and increments the position
            cardDisplay += "| " + cards.get(pos).getDescription() + " ";
            pos += 1;
            // If the 10 cards have been added:
            // Add a line of dashes and start adding the next 10 cards
            if (pos % cardsPerLine == 0) {
                cardDisplay += "|" + System.lineSeparator() + lineOfDashes + System.lineSeparator();
            }
            // All the cards have been added to the display
            if (pos == cards.size()) {
                complete = true;
            }
        }

        // If the last row is not filled with cards:
        // The last bar "|", and dashes have not been added in the preceding loop
        if (cards.size() % cardsPerLine > 0) {
            cardDisplay += "|" + System.lineSeparator();
            // If there are less than 10 cards in the collection: then the lineOfDashes already holds the correct amount of dashes
            // If there are more than 10 cards in the collection, the lineOfDashes holds 10 dashes and needs to be adjusted
            if (cards.size() > cardsPerLine) {
                lineOfDashes = createLineOfDashes(cards.size() % cardsPerLine);
            }
            cardDisplay += lineOfDashes + System.lineSeparator();
        }
        return cardDisplay;
    }
}