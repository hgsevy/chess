package clientTests;

import server.Server;

import ui.TerminalMenus;


public class UserTest {

    private static Server server;

    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port + '\n');
    }


    public static void main(String[] args) {
        //init();

        TerminalMenus thing = new TerminalMenus(8080);
        thing.runThis();

        server.stop();
    }

}
