package CanifisRooftops;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;

import static org.dreambot.api.methods.tabs.Tabs.logout;


//TODO: break manager

@ScriptManifest(author = "Brotato", category = Category.AGILITY, description = "Canifis Agility Course", name = "Canifis Agility", version = 2.0)
public final class CanifisRooftops extends AbstractScript implements ChatListener {

    // --__--__--__--__--__--__--__--__--__--__--__--__--__--
    // __--Filters and variables_--__--__--__--__--__--__--__
    // --__--__--__--__--__--__--__--__--__--__--__--__--__--
    private final Area bankArea = new Area(3509, 3483, 3513, 3477);
    private final Area startArea = new Area(3504, 3488, 3508, 3484);

    private final Area troubleArea = new Area(3496, 3507, 3504, 3503, 2);
    //   private final Area longRoofArea =  new Area(3222, 3401, 3227, 3395, 3);
    private boolean slowMode;
    private int marksCollected;

    private int oldAgilityExp = 0;
    private int lapsCompleted;
    private int maxLapsCondition = 20;
    private long startTime = 0;

    @Override
    public void onStart() {
        doActionsOnStart();
    }

    @Override
    public void onExit() {
        doActionsOnExit();
    }

    @Override
    public int onLoop() {
        performLoopActions();
        return nextInt(60, 75);
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString("Laps Completed: " + lapsCompleted, 12, 300);
        g.drawString("Run time: " + getElapsedTimeAsString(), 12, 240);
        g.drawString("Agility Lvl: " + Skills.getRealLevel(Skill.AGILITY), 12, 220);
        g.drawString("Exp/Hr: " + SkillTracker.getGainedExperiencePerHour(Skill.AGILITY), 12, 200);
        g.drawString("Time Till Level: " + makeTimeString(SkillTracker.getTimeToLevel(Skill.AGILITY)), 12, 180);
        g.drawString("Marks Collected: " + marksCollected, 12, 160);


    }


    @Override
    public void onPlayerMessage(Message msg) {
        handlePlayerMessage(msg);
    }

    @Override
    public void onMessage(Message msg) {
        handleGameMessages(msg);
    }


    private void doActionsOnStart() {
        startTime = System.currentTimeMillis();
        SkillTracker.start(Skill.AGILITY);
        oldAgilityExp = Skills.getExperience(Skill.AGILITY);
        Walking.setRunThreshold(nextInt(81, 92));
    }

    private void doActionsOnExit() {
        log(String.format("Gained agility xp: %d", (Skills.getExperience(Skill.AGILITY) - oldAgilityExp)));
        log("Runtime: " + getElapsedTimeAsString());
    }




    // --__--__--__--__--__--__--__--__--__--__--__--__--__--
    // __--Helper functions__--__--__--__--__--__--__--__--__
    // --__--__--__--__--__--__--__--__--__--__--__--__--__--

    private String getElapsedTimeAsString() {
        return makeTimeString(getElapsedTime()); //make a formatted string from a long value
    }

    private long getElapsedTime() {
        return System.currentTimeMillis() - startTime; //return elapsed millis since start of script
    }


    private String makeTimeString(long ms) {
        final int seconds = (int) (ms / 1000) % 60;
        final int minutes = (int) ((ms / (1000 * 60)) % 60);
        final int hours = (int) ((ms / (1000 * 60 * 60)) % 24);
        final int days = (int) ((ms / (1000 * 60 * 60 * 24)) % 7);
        final int weeks = (int) (ms / (1000 * 60 * 60 * 24 * 7));
        if (weeks > 0) {
            return String.format("%02dw %03dd %02dh %02dm %02ds", weeks, days, hours, minutes, seconds);
        }
        if (weeks == 0 && days > 0) {
            return String.format("%03dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        }
        if (weeks == 0 && days == 0 && hours > 0) {
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        }
        if (weeks == 0 && days == 0 && hours == 0 && minutes > 0) {
            return String.format("%02dm %02ds", minutes, seconds);
        }
        if (weeks == 0 && days == 0 && hours == 0 && minutes == 0) {
            return String.format("%02ds", seconds);
        }
        if (weeks == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return String.format("%04dms", ms);
        }
        return "00";
    }

    private void handlePlayerMessage(Message msg) {
        log(String.format("%d, %d", msg.getTime(), msg.getTypeID()));
    }

    private void handleGameMessages(Message msg) {
        log(msg);
    }

    private void slowMode() {
        slowMode = (lapsCompleted % 2 == 0 && lapsCompleted != 0);
    }



    private int nextInt(int lowValIncluded, int highValExcluded) { //get a random value between a range, high end is not included
        return ThreadLocalRandom.current().nextInt(lowValIncluded, highValExcluded);
    }

    private Player player() { //get the local player, less typing
        return Players.getLocal();
    }

    private int playerX() { //get player x location
        return player().getX();
    }

    private int playerY() { //get player y location
        return player().getY();
    }

    private int playerZ() { //get player z location
        return player().getZ();
    }

    private boolean isMoving() { //true if player is moving
        return player().isMoving();
    }

    private boolean isAnimating() {
        return player().isAnimating();
    }

    private boolean atStartArea() {
        return startArea.contains(player());
    }
    private boolean atBankArea() {
        return bankArea.contains(player());
    }

    private void checkOnScreen(GameObject obstacle){
        if (!obstacle.isOnScreen()){
            Camera.mouseRotateToEntity(obstacle);
            sleep(220,440);
        }
    }
    private void handleDialogues() {
        if (Dialogues.inDialogue()) { // see https://dreambot.org/javadocs/org/dreambot/api/methods/dialogues/Dialogues.html
            for (int i = 0; i < 4; i++) {
                if (Dialogues.canContinue()) { //
                    Dialogues.continueDialogue(); //
                    sleep(nextInt(500, 750)); //
                } else {
                    break; //break out of loop, if no more dialogues
                }
            }
        }
    }

    private void checkHealth(){
        log("health check is good, continuing...");
        if (player().getHealthPercent() < 50){
            log("LOW HP - Logging out");
            stop();
            logout();
        }
        if (player().getHealthPercent() < 70){
            log("Below 70% health, eating...");
            Inventory.interact("Lobster", "Eat");
        }
    }
    private void getFoodIfNone(){
        log("food situation is good, continuing...");
        if (Inventory.count("Lobster") > 1){
            log("Still have food, continuing...");
        }
        else {
            Walking.walk(bankArea);
            sleepUntil(() -> bankArea.contains(player()), 5000, 1000);
            if (Bank.open()){
                Bank.withdraw("Lobster", 5);
                sleep(1000,3000);
                if (Inventory.count("Lobster") >= 5){
                    clickFirstObstacle();
                }else {
                    log("fatal error, this should not occur");
                }

            }
        }
    }

    private void checkLapCondition() {
        if (lapsCompleted >= maxLapsCondition) {
            log("Max reasonable humanlike playtime reached -- logging out.");
            stop();
            logout();
        }
    }
    private void handlePlayersNearby(){
        if (playersNearby()){
            Player p = Players.closest(Objects::nonNull);
            if (p.distance() <= 9){
                log("Player within 9 tiles, trying to right click...");

                Mouse.click(p,true);
                sleep(900,1200);
            }
            //no hop for agi
//            World w = Worlds.getRandomWorld(wo -> !wo.isPVP() && !wo.isF2P()
//                    && !wo.isFreshStart() && !wo.isTournamentWorld() && wo.isMembers() && wo.getMinimumLevel() == 0);
//            log("Randomnly selected world " + w);
//            WorldHopper.hopWorld(w);
        }
    }
    private boolean playersNearby(){
        int x = Players.all().size();
        log("Players nearby: " + x);
        return x > 0;
    }
    //Main functions
    private void performLoopActions() {
        if (ScriptManager.getScriptManager().isRunning() && Client.isLoggedIn()) {
            handlePlayersNearby();

            playersNearby();
            checkHealth();
            getFoodIfNone();
            slowMode();
            checkLapCondition();
            handleDialogues();
            checkIfWeFell();
            climbTreeToStart();
            firstAfterTree();
            gapNumberOne();
            gapNumberTwo();
            gapNumberThree();
            jumpGapAfterWall();
            gapNumberFour();
            lastObstacle();
        }
    }
    private void checkForMarks() {
        if (Inventory.isFull()) {
            log("Full inventory -- please fix");
            return;
        }
        int currentMarks = Inventory.count("Mark of grace");
        GroundItem mark = GroundItems.closest("Mark of Grace");
        if (mark != null) {
            log("Found mark of grace -- current count: " + currentMarks);
            sleep(nextInt(1500, 3000));
            if (Walking.canWalk(mark))
                if (mark.interact("Take")) {
                    sleepUntil(() -> Inventory.count("Mark of grace") == currentMarks+1, nextInt(1500, 3500), 1000);
                    log("Found and looted mark of grace");
                    marksCollected++;

                }
        }
    }


    private void checkIfWeFell() {
        if (playerZ() == 0 && !atStartArea() && !atBankArea()) {
            log("We fell ... attempting to restart course...");
            if (!startArea.contains(player())) {
                Walking.walk(startArea);
                sleepUntil(
                        () -> (player().distance(Walking.getDestination()) <= nextInt(3, 5)),
                        () -> isMoving(),
                        nextInt(3600, 4000), //timer duration
                        nextInt(320, 480)); //every time, poll timer is up, check reset condition. If true, then reset timer duration

            }
        }
    }



    private void clickFirstObstacle(){
        GameObject tree = GameObjects.closest(14843);
        if (tree != null){
            Walking.walk(startArea);
            sleepUntil(() -> startArea.contains(player()), 5500, 1000);
            if (tree.interact()){
                sleep(500,900);
            }
        }
    }

    private void climbTreeToStart() {
        if (atStartArea()) {
            log("At start area -- beginning course...");
            final GameObject tree = GameObjects.closest(14843);

            if (tree != null) {
                checkOnScreen(tree);
                if (tree.distance() > 9) {
                    Walking.walk(tree);
                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);

                    sleepUntil(
                            () -> (player().distance(Walking.getDestination()) <= nextInt(3, 5)),
                            () -> isMoving(),
                            nextInt(3600, 4000),
                            nextInt(320, 480));
                }
                if (tree.interact()) {
                    sleepUntil(
                            () -> playerZ() == 2,
                            () -> isMoving(),
                            nextInt(1000, 2000),
                            nextInt(320, 480)
                    );
                }
                if (slowMode) {
                    log("slow mode detected, sleeping 6-10s");
                    sleep(nextInt(6000, 10000)); // Every 2nd lap, take longer breaks between obstacles
                }
            }
        }
    }
    private void firstAfterTree(){
        if (playerZ() == 2) {
            log("coords good");
            if (slowMode) {
                log("slow mode true -- sleeping");
                sleep(nextInt(2000, 3500));
            }
            checkForMarks();
            final GameObject gapOne = GameObjects.closest(14844);
            if (gapOne != null) {
                checkOnScreen(gapOne);
                if (gapOne.interact()) {
                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);

                    sleepUntil(
                            () -> playerY() == 3504,
                            () -> isMoving(),
                            nextInt(1000, 2000),
                            nextInt(320, 480)
                    );
                }
            }
        }else {
            log("coords might b wrong");
        }
    }


    private void gapNumberOne() {
        if (troubleArea.contains(player())) {
            log("SLEEPING");
            sleep(5000,6000);
            if (slowMode) {
                log("slow mode true -- sleeping");
                sleep(nextInt(2000, 3500));
            }
            checkForMarks();
            final GameObject differentName = GameObjects.closest(14845);
            if (differentName != null) {
                checkOnScreen(differentName);
                if (differentName.interact()) {
//                    sleep(2500,5000);
                    log("should only click next");
                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);

                    sleepUntil(
                            () -> playerY() == 3504 || playerY() == 3503,
                            nextInt(1000, 2000),
                            nextInt(320, 480)
                    );
                    log("hit 3504");
                }
            }
        }else {
            log("coords might b wrong");
        }

    }

    private void gapNumberTwo() {
        if ((playerZ() == 2 && playerY() == 3504 && playerX() == 3492) || (playerZ() == 2 && playerY() == 3503 && playerX() == 3492)) {
            checkForMarks();
            log("coords good");

            final GameObject secondGap = GameObjects.closest(14848);
            if (secondGap != null) {
                checkOnScreen(secondGap);
                if (secondGap.interact()) {

                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);
                    sleepUntil(
                            () -> playerZ() == 3,5000,1000
                    );
                }
                if (slowMode) {
                    log("slow mode true -- sleeping");
                    sleep(nextInt(3000, 22000)); // Every 2nd lap, take longer breaks between obstacles
                }
            }
        }
        else {
            log("coords might b wrong");
        }
    }



    private void gapNumberThree() {
        if ((playerZ() == 3 && playerY() == 3499 && playerX() == 3479) || (playerZ() == 3 && playerY() == 3498 && playerX() == 3479)) {
            checkForMarks();
            log("coords good");
            if (slowMode) {
                log("slow mode true -- sleeping");
                sleep(nextInt(1500, 3000));
            }
            final GameObject thirdGap = GameObjects.closest(14846);
            if (thirdGap != null) {
                checkOnScreen(thirdGap);

                if (thirdGap.interact()) {
                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);

                    sleepUntil(
                            () -> playerZ() == 2,
                            () -> isMoving(),
                            nextInt(4000, 7000),
                            nextInt(320, 480)
                    );
                }
            }
        }
        else {
            log("coords might b wrong");
        }
    }

    private void jumpGapAfterWall() {
        if (playerZ() == 2 && playerX() == 3478 && playerY() == 3486) {
            checkForMarks();
            log("coords good");
            final GameObject longGap = GameObjects.closest(14894);
            if (longGap != null) {
                checkOnScreen(longGap);
                if (longGap.interact()) {
                    sleepUntil(() -> (playerZ() == 3), nextInt(3000, 3500), 1000);
                    sleepUntil(this::isMoving, nextInt(500, 1000), 1000);

                }
            }
        }
        else {
            log("coords might b wrong");
        }

    }

    private void gapNumberFour() {
        if ((playerZ() == 3 && playerY() == 3476 && playerX() == 3489) || (playerZ() == 3 && playerY() == 3476 && playerX() == 3487)
                || (playerZ() == 3 && playerY() == 3476 && playerX() == 3488)) {
            sleep(250,700);
            checkForMarks();
            log("coords good");
            final GameObject pole = GameObjects.closest(14847);
            if (pole != null) {
                checkOnScreen(pole);
                if (pole.interact()) {
                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);
                    sleepUntil(

                            () -> playerZ() == 2, //we succeeded
                            () -> (isMoving() || isAnimating()),
                            nextInt(1000, 2000),
                            nextInt(320, 480)
                    );
                }
                if (slowMode) {
                    log("slow mode true -- sleeping");
                    sleep(nextInt(2000, 3500)); // Every 2nd lap, take longer breaks between obstacles
                }
            }
        }
        else {
            log("coords might b wrong");
        }
    }

    public void lastObstacle() {
        if (playerZ() == 2 && playerY() == 3476 && playerX() == 3510) {
            log("coords good");
            checkForMarks();
            final GameObject lastGap = GameObjects.closest(14897);
            if (lastGap != null) {
                checkOnScreen(lastGap);
                if (lastGap.interact()) {
                    sleepUntil(() -> isMoving(), nextInt(500, 1000), 1000);
                    sleepUntil(() -> playerZ() == 0, nextInt(600, 1800), 1000);
                    lapsCompleted++;
                }
            }
        }
        else {
            log("coords might b wrong");
        }
    }
}