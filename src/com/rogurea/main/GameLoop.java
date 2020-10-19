package com.rogurea.main;

import com.googlecode.lanterna.input.KeyType;
import com.rogurea.main.gamelogic.Scans;
import com.rogurea.main.items.Weapon;
import com.rogurea.main.map.Dungeon;
import com.rogurea.main.map.Room;
import com.rogurea.main.mapgenerate.BaseGenerate;
import com.rogurea.main.player.KeyController;
import com.rogurea.main.player.Player;
import com.rogurea.main.player.PlayerMoveController;
import com.rogurea.main.resources.GameResources;
import com.rogurea.main.view.Log;
import com.rogurea.main.view.TerminalView;

import java.io.IOException;
import java.util.Objects;

import static com.rogurea.main.player.Player.CurrentRoom;
import static com.rogurea.main.player.Player.PlayerModel;

public class GameLoop {

    public static void Start(){
        try{
            TerminalView.InitTerminal();
            InLoop();

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            if (TerminalView.terminal != null) {
                try {
                    TerminalView.terminal.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void InLoop() throws IOException {

        Thread drawcall = new Thread(new TerminalView(), "drawcall");

        drawcall.start();

        /*Runnable r = () -> {
            for(int i = 0; i < GameResources.ProgessBar.length; i++) {
                Dungeon.CurrentRoom[3][3] = GameResources.ProgessBar[i];
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };*/
        //new BotController_3().start();

        while (TerminalView.keyStroke.getKeyType() != KeyType.Escape) {

            Player.AutoEquip();

            TerminalView.keyStroke = TerminalView.terminal.readInput();

/*            if(R_MobController.MobsScan()) {
                T_View.DrawFightMenu();
                T_View.MenuPrompt(R_Dungeon.CurrentRoomCreatures.keySet());
            }*/

            if (TerminalView.keyStroke.getKeyType() == KeyType.Character) {
                KeyController.GetKey(TerminalView.keyStroke.getCharacter());
            }
            PlayerMoveController.MovePlayer(TerminalView.keyStroke.getKeyType());

            Scans.CheckSee(Dungeon.CurrentRoom[Player.Pos.y+1][Player.Pos.x]);
        }
    }

    public static void RegenRoom() throws IOException {
        System.out.flush();
        BaseGenerate.GenerateRoom(Objects.requireNonNull(BaseGenerate.GetRoom(Dungeon.Direction.FIRST)));
        BaseGenerate.PutPlayerInDungeon(Dungeon.CurrentRoom[0].length/2,1, Dungeon.CurrentRoom);
    }

    public static void ChangeRoom(Room room){
        if(!room.IsRoomStructureGenerate){
            try {
                BaseGenerate.GenerateRoom(
                        Objects.requireNonNull(BaseGenerate.GetRoom(Dungeon.Direction.NEXT)).nextRoom);
            }
            catch (NullPointerException | IOException e){
                e.getMessage();
                Dungeon.CurrentRoom[1][1] = PlayerModel;
            }
            finally {
                BaseGenerate.PutPlayerInDungeon(
                        BaseGenerate.GetCenterOfRoom(room), 1,
                        Dungeon.CurrentRoom);
            }
        }
        else{
            com.rogurea.main.player.Player.CurrentRoom = room.NumberOfRoom;
            Dungeon.CurrentRoom = room.RoomStructure;
            BaseGenerate.PutPlayerInDungeon(BaseGenerate.GetCenterOfRoom(room), 1,
                    room.RoomStructure);
        }
    }

}
