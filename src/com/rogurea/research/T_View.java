package com.rogurea.research;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Set;

public class T_View extends Thread{


    static int OffsetPositionName = 1;

    static int FightMenuSizeRows = 5;

    static DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

    static Terminal terminal = null;

    static TextGraphics MapDrawGraphics = null;

    static TextGraphics PlayerInfoGraphics = null;

    static TextGraphics FightInfoGraphics = null;

    static KeyStroke keyStroke = KeyStroke.fromString(" ");

    static TerminalPosition topPlayerInfoLeft;

    static TerminalPosition topFightInfoLeft;

    static TerminalSize PlayerInfoSize = new TerminalSize(getPlayerPositionInfo().length() + 2, 5);

    static TerminalSize FightMenuSize = new TerminalSize(OffsetPositionName, FightMenuSizeRows);


    static String getPlayerPositionInfo(){
        return  "Player position "
                + "x:" + R_Player.Pos.x
                + " "
                + "y:" + R_Player.Pos.y + ' ';
    }

    static void SetGameScreen() throws IOException {

        MapDrawGraphics = terminal.newTextGraphics();

        PlayerInfoGraphics = terminal.newTextGraphics();

        FightInfoGraphics = terminal.newTextGraphics();

        terminal.resetColorAndSGR();
    }

    public static void InitTerminal(){
        try {
            terminal = defaultTerminalFactory.createTerminal();

            terminal.flush();

            terminal.enterPrivateMode();

            terminal.clearScreen();

            terminal.setCursorVisible(false);

            SetGameScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void Drawcall() throws IOException {
        ResetTerminalPosition();

        InitGraphics(PlayerInfoGraphics, topPlayerInfoLeft, PlayerInfoSize);

        DrawPlayerInformation();

        DrawDungeon();

        terminal.flush();
    }

    static void ResetTerminalPosition(){

        topPlayerInfoLeft = new TerminalPosition(R_Dungeon.CurrentRoom.length + 1,1);

        topFightInfoLeft = new TerminalPosition(R_Dungeon.CurrentRoom.length + 1,
                topPlayerInfoLeft.getRow()+1);

        FightMenuSize = new TerminalSize(OffsetPositionName, FightMenuSizeRows);

/*        System.out.println("topLeft: " +
                topPlayerInfoLeft.toString() + " topFightLeft: "
                + topFightInfoLeft.toString() + " FightMenuSize: "
                + FightMenuSize.toString());*/
    }

    static void InitGraphics(TextGraphics textGraphics, TerminalPosition terminalPosition, TerminalSize terminalSize){
        textGraphics.fillRectangle(terminalPosition, terminalSize, ' ');
    }

    static void DrawPlayerInformation(){

        PlayerInfoGraphics.putString(topPlayerInfoLeft.withRelative(1, 1),
                getPlayerPositionInfo());

        PlayerInfoGraphics.putString(topPlayerInfoLeft.withRelative(1,2),
                "Room size: " + R_Dungeon.CurrentRoom.length + "x" + R_Dungeon.CurrentRoom[0].length);

        PlayerInfoGraphics.putString(topPlayerInfoLeft.withRelative(1,3), "" +
                "Player: " + R_Player.nickName + " "
                + "HP:" + R_Player.HP + " "
                + "MP:" + R_Player.MP + " "
                + "Level:" + R_Player.Level + " "
                + "Room:" + R_Player.CurrentRoom);
    }

    static void DrawDungeon(){
        char cell;
        for (int i = 0; i < R_Dungeon.CurrentRoom.length; i++)
            for (int j = 0; j < R_Dungeon.CurrentRoom[0].length; j++) {
                cell = R_Dungeon.ShowDungeon(i,j).charAt(0);
                if(cell == R_Player.Player) {
                    DrawPlayer(MapDrawGraphics, i, j);
                }
                else if(CheckCreature(cell)){
                    DrawMob(MapDrawGraphics, i, j);
                }
                else{
                    MapDrawGraphics.putString(i, j, R_Dungeon.ShowDungeon(i, j));
                }
                if (j == R_Dungeon.CurrentRoom[0].length - 1)
                    MapDrawGraphics.putString(i, j, "\n", SGR.BOLD);
            }
    }
    static void DrawPlayer(TextGraphics textGraphics, int i, int j){
        textGraphics.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
        textGraphics.putString(i, j, R_Dungeon.ShowDungeon(i, j), SGR.CIRCLED);
        textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
    }

    static boolean CheckCreature(char cell){

        for(R_Mob mob : R_Dungeon.CurrentRoomCreatures.keySet()){
            if(cell == mob.getCreatureSymbol())
                return true;
        }
        return false;
    }

    static void DrawMob(TextGraphics textGraphics, int i, int j){
        textGraphics.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
        textGraphics.putString(i, j, R_Dungeon.ShowDungeon(i, j), SGR.CIRCLED);
        textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
    }
/*

    static void FightTextGraphics(TextGraphics fightGraphics, TerminalPosition topFightLeft){
        fightGraphics.fillRectangle(topFightLeft, FightMenuSize, ' ');
        FightMenuSize = new TerminalSize(Offset, 1);
    }

    static void SlideCursor(int NameOffsetA, int NameOffsetB){

        fightGraphics.setCharacter(
                topFightLeft.withRelative(NameOffsetA, 4), ' '
        );
        SetCursor(NameOffsetB);
    }

    static void SetCursor(int NameOffset){
        fightGraphics.setCharacter(
                topFightLeft.withRelative(NameOffset,4),
                Symbols.TRIANGLE_RIGHT_POINTING_BLACK);
    }

    static R_Mob SetMob(int MobIndex, Set<R_Mob> CurrentRoomMobSet){
        return (R_Mob) CurrentRoomMobSet.toArray()[MobIndex];
    }

    static int PutMobNames(String mobname){
        fightGraphics.putString(topFightLeft.withRelative(Offset,4), mobname);
        return Offset += mobname.length()+2;
    }

    static void ResetFightMenuSize(){
        FightMenuSize = new TerminalSize(R_Dungeon.CurrentRoom.length+MaxOffset, FightMenuSizeRows);
    }

    static void ClearFightMenu(int offset){
        fightGraphics.fillRectangle(new TerminalPosition(
                R_Dungeon.CurrentRoom.length+offset, topLeft.getRow()+5), FightMenuSize, ' ');
    }

    static void ResetMobNames(int[] NameOffset, Set<R_Mob> CurrentRoomMobSet){
        Offset = 1;
        int Index = 0;

        ResetFightMenuSize();
        ClearFightMenu(2);

        for(R_Mob mob : CurrentRoomMobSet){
            NameOffset[Index] = PutMobNames(mob.Name)-mob.Name.length()-3;
            Index++;
        }
        SetCursor(NameOffset[MobIndex]);
    }

    static void MenuPrompt(Set<R_Mob> CurrentRoomMobSet) throws IOException {

        int[] NameOffset = new int[CurrentRoomMobSet.size()];

        R_Mob CurrentMob;

        KeyStroke KeyMenu;

        */
/*System.out.println("In fight: \n" + "\ttopLeft: " +
                topLeft.toString() + " topFightLeft: "
                + topFightLeft.toString() + " FightMenuSize: "
                + FightMenuSize.toString() + " topRight: "
                + topRight.toString());*//*


        ResetMobNames(NameOffset, CurrentRoomMobSet);

        MaxOffset = Offset;

        SetCursor(NameOffset[0]);

        terminal.flush();

        while(!CurrentRoomMobSet.isEmpty()){

            DrawFightMenu();

            ResetMobNames(NameOffset, CurrentRoomMobSet);

            terminal.flush();

            KeyMenu = terminal.readInput();

            try {
                switch (KeyMenu.getKeyType()) {
                    case ArrowRight -> {
                        MobIndex++;
                        SlideCursor(NameOffset[MobIndex - 1], NameOffset[MobIndex]);
                        //CurrentMob = SetMob(MobIndex, CurrentRoomMobSet);
                    }
                    case ArrowLeft -> {
                        MobIndex--;
                        SlideCursor(NameOffset[MobIndex + 1], NameOffset[MobIndex]);

                        //CurrentMob = SetMob(MobIndex, CurrentRoomMobSet);
                    }
                    case Enter -> {
                        CurrentMob = SetMob(MobIndex, CurrentRoomMobSet);
                        R_MobController.RemoveMob(CurrentMob);
                        CurrentRoomMobSet.remove(CurrentMob);
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException e){
                SlideCursor(NameOffset[Math.abs(MobIndex)-MobIndex], 0);
                MobIndex = 0;
            }
        }
        ClearFightMenu(1);
        terminal.flush();
    }

    static void DrawFightMenu() throws IOException {

        Offset = 1;

        fightGraphics.putString(topFightLeft.withRelative(1, 3),
                "Creatures:");
    }






*/
}
