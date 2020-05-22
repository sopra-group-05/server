# SoPra FS20 - Just One M4 - Server

## Introduction

Welcome to the digital version of the game Just One! Players are able to register and login and get a good look
at the dashboard. Users can join other lobbies, start a game with different sizes of decks, or play in 
English or German against other Players in real time. There's also a game mode where you can play against Bots. 

Players can also compare each other via a leader Board. Their score will be set according to the 
time they need to come up with the clues and guesses and whether they were eliminated or not.

We are using ReactJS in the Frontend and Java (Spring Boot) in the Backend. The Frontend will interact with the Backend
via a REST API. We decided to consume different external APIs, one in the form of synonyms and antonyms for the Bots
and another to show a definition of a mystery word (if a player doesn’t know what it is).

## Technologies used
We're using Spring Boot for the Backend of Just One. 

Getting started with Spring Boot:

-   Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/index.html
-   Guides: http://spring.io/guides
    -   Building a RESTful Web Service: http://spring.io/guides/gs/rest-service/
    -   Building REST services with Spring: http://spring.io/guides/tutorials/bookmarks/

## High-Level components [todo]

In this section we'll tell you about three main components and their role.

todo for backend!

## Launch & Deployment 
### Setup this Template with your IDE of choice

Download your IDE of choice: (e.g., [Eclipse](http://www.eclipse.org/downloads/), [IntelliJ](https://www.jetbrains.com/idea/download/)) and make sure Java 13 is installed on your system.

1. File -> Open... -> SoPra Server Template
2. Accept to import the project as a `gradle project`

To build right click the `build.gradle` file and choose `Run Build`

### Building with Gradle

You can use the local Gradle Wrapper to build the application.

Plattform-Prefix:

-   MAC OS X: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/).

#### Build

```bash
./gradlew build
```

#### Run

```bash
./gradlew bootRun
```

#### Test

```bash
./gradlew test
```

#### Development Mode

You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed and you save the file.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

### API Endpoint Testing

#### Postman

-   We highly recommend to use [Postman](https://www.getpostman.com) in order to test your API Endpoints.

### Debugging

If something is not working and/or you don't know what is going on. We highly recommend that you use a debugger and step
through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command),
do the following:

1. Open Tab: **Run**/Edit Configurations
2. Add a new Remote Configuration and name it properly
3. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
4. Press `Shift + F9` or the use **Run**/Debug"Name of your task"
5. Set breakpoints in the application where you need it
6. Step through the process one step at a time

### Testing

Have a look here: https://www.baeldung.com/spring-boot-testing

## Roadmap

Here are two features that you, as a new member of our team, could contribute to this project!

### Adding custom Mystery-Word Cards

As a User I want to be able to add mystery word cards to the Game in order to keep the game interesting and avoid 
playing the same words over and over again.
- On the Lobby-Page there should be a button that opens a add-mystery-word-card form
- In the form there should be five empty spaces to put five mystery words.
- The mystery word should be put to the list of mystery-word-cards.
- The user should be able to specify to which language the card should belong.

### Let the active Player choose which Clue was the best one

As an active Player, I want to be able to choose the best clue presented in order to award the player who wrote 
it some extra points.
- Alle Clues should be visible to the active Player.
- The active Player should be able to click on the best clue.
- The Player that wrote the chosen clue should get extra points.
- The active Player should be able to skip if no clue stands out.

### Frontend German Language

As a user if I join a lobby that plays in German I want to play the whole game in German (and not just the words).
- Translated text to German for all content inside a lobby.

## Authors and acknowledgement

- [Adiboeh](https://github.com/Adiboeh)
- [Floribur](https://github.com/Floribur)
- [nmulle](https://github.com/nmulle)
- [yritz](https://github.com/yritz)
- [mgoki](https://github.com/mgoki)
- [InfoYak](https://github.com/InfoYak)

## License

[GNU GPLv3](https://choosealicense.com/licenses/gpl-3.0/) 