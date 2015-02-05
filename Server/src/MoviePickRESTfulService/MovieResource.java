package MoviePickRESTfulService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.resteasy.spi.NoLogWebApplicationException;

@Path("/movie")
public class MovieResource{
  
  public static final Map<Integer, Movie> movieDB = new HashMap<Integer, Movie>();
  
  @POST
  @Consumes( MediaType.APPLICATION_JSON )
  public Response createEntryJSON( Movie movie ){
    System.out.println( "MovieResource.createEntry" );
    for(Map.Entry<Integer, Movie> entry : movieDB.entrySet()){
      if(entry.getValue().equals(movie)){
        return Response.status(Response.Status.FOUND).build(); 
      }
    }
    Integer id = movieDB.size() + 1;
    movieDB.put(id, movie);
    return Response.created( URI.create("/movie/" + id) ).build();
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<Integer, Movie> returnListJSON(){
    return movieDB;
  }
  
  @GET
  @Path( "{id: [1-9][0-9]*}" )
  @Produces(MediaType.APPLICATION_JSON)
  public Movie getEntryJSON(@PathParam("id") Integer id){
    final Movie movie = movieDB.get(id);
    if(movie == null){
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return movie;
  }
  
  @PUT
  @Path("{id: [1-9][0-9]*}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateMovieJSON(@PathParam("id") Integer id, Movie movie){
    Movie current = movieDB.get(id);
    if(current == null){
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    current.setMovieTitle(movie.getMovieTitle());
    current.setGenre(movie.getGenre());
    current.setRate((current.getRate() + movie.getRate())/2);
    
    return Response.ok().build();
  }
  
  @DELETE
  @Path("{id: [1-9][0-9]*}")
  public Response deleteMovie(@PathParam("id") Integer id){
    Movie movie = movieDB.get(id);
    if(movie == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    movieDB.remove(id);
    for(ShowTime time : TheaterResource.showTimes){
      if(time.getMovie().equals(movie)){
        TheaterResource.showTimes.remove(time);
      }
    }
    return Response.ok().build();
  }
  
  @GET
  @Path("{id: [1-9][0-9]*}/theater")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<ShowTime> getTheatersShowTimeForMovie(@PathParam("id") Integer id){
    ArrayList<ShowTime> shows = new ArrayList<ShowTime>();
    Movie movie = movieDB.get(id);
    if(movie == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    for(ShowTime show : TheaterResource.showTimes){
      if(show.getMovie().equals(movie)){
        shows.add(show);
      }
    }
    return shows;
  }
  
  @POST
  @Path("{id: [1-9][0-9]*}/theater")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response registerMovie2Theater(@PathParam("id") Integer movieId, IntStringArray theaterIdShows){
    Movie movie = movieDB.get(movieId);
    if(movie == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    int theaterId = theaterIdShows.getInteger();
    Theater theater = TheaterResource.theaterDB.get(theaterId);
    if(theater == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    ArrayList<String> shows = theaterIdShows.getStringArray();
    for(ShowTime time : TheaterResource.showTimes){
      if(time.getMovie().equals(movie) && time.getTheater().equals(theater)){
        //time.addAllTime(shows);
        return Response.status(Response.Status.FOUND).build(); 
      }
    }
    ShowTime newShowTime = new ShowTime(movie, theater, shows);
    TheaterResource.showTimes.add(newShowTime);
    return Response.created( URI.create("/movie/" + movieId + "/theater/" + theaterId) ).build();
  }
  
  @GET
  @Path("{id: [1-9][0-9]*}/theater/{id1: [1-9][0-9]*}")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<ShowTime> getShowTimesOfMovieInTheaterJSON(@PathParam("id") Integer movieId, @PathParam("id1") Integer theaterId){
    ArrayList<ShowTime> shows = new ArrayList<ShowTime>();
    Movie movie = movieDB.get(movieId);
    if(movie == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    Theater theater = TheaterResource.theaterDB.get(theaterId);
    if(theater == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    for(ShowTime show : TheaterResource.showTimes){
      if(show.getMovie().equals(movie) && show.getTheater().equals(theater))
        shows.add(show);
    }
    return shows;
  }
  
  @DELETE
  @Path("{id: [1-9][0-9]*}/theater/{id1: [1-9][0-9]*}")
  public Response deleteShowsJSON(@PathParam("id") Integer movieId, @PathParam("id1") Integer theaterId){
    Movie movie = movieDB.get(movieId);
    if(movie == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    Theater theater = TheaterResource.theaterDB.get(theaterId);
    if(theater == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    for(ShowTime time : TheaterResource.showTimes){
      if(time.getMovie().equals(movie) && time.getTheater().equals(theater)){
        TheaterResource.showTimes.remove(time);
      }
    }
    return Response.ok().build();
  }
  
  @GET
  @Path("genre/{genre: [A-Z][a-zA-Z]*}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<Integer, Movie> getMovieByGenre(@PathParam("genre") String genre){
    Map<Integer, Movie> movies = new HashMap<Integer, Movie>();
    for(Map.Entry<Integer, Movie> entry : movieDB.entrySet()){
      if(entry.getValue().getGenre().toLowerCase().compareTo(genre.toLowerCase()) == 0){
        movies.put(entry.getKey(), entry.getValue());
      }
    }
    return movies;
  }
  
  @GET
  @Path("rate/{rate: [1-5]}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<Integer, Movie> getMovieByRate(@PathParam("rate") Integer rate){
    Map<Integer, Movie> movies = new HashMap<Integer, Movie>();
    for(Map.Entry<Integer, Movie> entry : movieDB.entrySet()){
      if(entry.getValue().getRate() == rate){
        movies.put(entry.getKey(), entry.getValue());
      }
    }
    return movies;
  }
}
