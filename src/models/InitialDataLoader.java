//package models;
//
//import services.SpotifyService;
//import models.*;
//import java.util.HashSet;
//import java.util.Set;
//
//public class InitialDataLoader {
//    public static void loadInitialData(SpotifyService spotifyService) {
//        //objects instatied here for readability of code - every data initialized at runtime will be written here
//        //because there is no database yet and the user-made objects will not be saved
//        //artists
//        Artist maggieLindemann = new Artist();
//        maggieLindemann.setArtistNickname("Maggie Lindemann");
//        maggieLindemann.setArtistAge(25);
//        spotifyService.artists.add(maggieLindemann);
//
//        Artist victorRay = new Artist();
//        victorRay.setArtistNickname("Victor Ray");
//        victorRay.setArtistAge(23);
//        spotifyService.artists.add(victorRay);
//
//        Artist fletcher = new Artist();
//        fletcher.setArtistNickname("Fletcher");
//        fletcher.setArtistAge(30);
//        spotifyService.artists.add(fletcher);
//
//        //songs
//        Song intro = new Song();
//        intro.setSongYear(2022);
//        intro.setSongTime(76);
//        intro.setSongTitle("intro / welcome in");
//        intro.setSongArtist(maggieLindemann);
//        spotifyService.songs.add(intro);
//
//        Song takeMeNowhere = new Song();
//        takeMeNowhere.setSongYear(2022);
//        takeMeNowhere.setSongTime(175);
//        takeMeNowhere.setSongTitle("take me nowhere");
//        takeMeNowhere.setSongArtist(maggieLindemann);
//        spotifyService.songs.add(takeMeNowhere);
//
//        Song sheKnowsIt = new Song();
//        sheKnowsIt.setSongYear(2022);
//        sheKnowsIt.setSongTime(157);
//        sheKnowsIt.setSongTitle("she knows it");
//        sheKnowsIt.setSongArtist(maggieLindemann);
//        spotifyService.songs.add(sheKnowsIt);
//
//        Song casualty = new Song();
//        casualty.setSongYear(2022);
//        casualty.setSongTime(197);
//        casualty.setSongTitle("casualty of your dreams");
//        casualty.setSongArtist(maggieLindemann);
//        spotifyService.songs.add(casualty);
//
//        Song stayForAWhile = new Song();
//        stayForAWhile.setSongYear(2023);
//        stayForAWhile.setSongTime(190);
//        stayForAWhile.setSongTitle("Stay For A While");
//        stayForAWhile.setSongArtist(victorRay);
//        spotifyService.songs.add(stayForAWhile);
//
//        Song comfortable = new Song();
//        comfortable.setSongYear(2024);
//        comfortable.setSongTime(229);
//        comfortable.setSongTitle("Comfortable");
//        comfortable.setSongArtist(victorRay);
//        spotifyService.songs.add(comfortable);
//
//        Song itOnlyCostsEverything = new Song();
//        itOnlyCostsEverything.setSongYear(2023);
//        itOnlyCostsEverything.setSongTime(148);
//        itOnlyCostsEverything.setSongTitle("It Only Costs Everything");
//        itOnlyCostsEverything.setSongArtist(victorRay);
//        spotifyService.songs.add(itOnlyCostsEverything);
//
//        Song bitter = new Song();
//        bitter.setSongYear(2020);
//        bitter.setSongTime(150);
//        bitter.setSongTitle("Bitter");
//        bitter.setSongArtist(fletcher);
//        spotifyService.songs.add(bitter);
//
//        //album
//        Song[] suckerpunchSongs = new Song[4];
//        suckerpunchSongs[0] = intro;
//        suckerpunchSongs[1] = takeMeNowhere;
//        suckerpunchSongs[2] = sheKnowsIt;
//        suckerpunchSongs[3] = casualty;
//        Album suckerpunch = new Album("SUCKERPUNCH", suckerpunchSongs, suckerpunchSongs.length, maggieLindemann, 2022);
//        spotifyService.albums.add(suckerpunch);
//
//
//        //genre album
//        Set<String> suckerpunchGenres = new HashSet<String>();
//        suckerpunchGenres.add("Pop");
//        suckerpunchGenres.add("Alternative/Indie");
//        GenreAlbum suckerPunch = new GenreAlbum("SUCKERPUNCH", suckerpunchSongs, suckerpunchSongs.length, maggieLindemann, 2022, suckerpunchGenres);
//        spotifyService.genreAlbums.add(suckerPunch);
//
//        //playlist
//        //victor ray's songs
//        Song[] victorSongs = new Song[3];
//        victorSongs[0] = stayForAWhile;
//        victorSongs[1] = comfortable;
//        victorSongs[2] = itOnlyCostsEverything;
//        Playlist victor = new Playlist(victorSongs, victorSongs.length);
//        victor.setPlaylistName("Victor Ray vibe");
//        spotifyService.playlists.add(victor);
//
//        //mixtape
//        //this should have two songs - the same - one from the album and one from the playlist
//        Mixtape maggieVictor = new Mixtape("maggie & victor", maggieLindemann, suckerpunchSongs.length , suckerpunchSongs, 2024, victorSongs.length, victorSongs);
//        spotifyService.mixtapes.add(maggieVictor);
//
//        //user and user account
//        UserAccount mara_account = new UserAccount("mara13", "1234");
//        spotifyService.user_accounts.add(mara_account);
//
//        User mara = new User("mara13", "1234", "Mara");
//        spotifyService.users.add(mara);
//
//
//        mara.addToLibrary(suckerpunch); //album
//        mara.addToLibrary(comfortable); //song
//        mara.addToLibrary(victor); //playlist
//    }
//}
