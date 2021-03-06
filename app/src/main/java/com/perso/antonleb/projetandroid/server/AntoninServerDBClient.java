package com.perso.antonleb.projetandroid.server;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.perso.antonleb.projetandroid.async.command.ICommandQueue;
import com.perso.antonleb.projetandroid.async.command.ListCommandQueue;
import com.perso.antonleb.projetandroid.datas.Category;
import com.perso.antonleb.projetandroid.datas.CategoryKey;
import com.perso.antonleb.projetandroid.datas.ICategory;
import com.perso.antonleb.projetandroid.datas.IUser;
import com.perso.antonleb.projetandroid.datas.User;
import com.perso.antonleb.projetandroid.datas.UserKey;
import com.perso.antonleb.projetandroid.datas.creators.SimplePolymorphCreator;
import com.perso.antonleb.projetandroid.exceptions.DBRequestException;
import com.perso.antonleb.projetandroid.receiver.NetworkStateReceiver;
import com.perso.antonleb.projetandroid.utils.ParcelableUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cédric DEMONGIVERT <cedric.demongivert@gmail.com>
 *
 * Un client vers un serveur qui respecte les standards d'Antonin.
 * Pas toujours accessible, plutôt grincheux et frustrant. Mais bon, bien plus fun que le local
 * faut bien avouer.
 */
public class AntoninServerDBClient implements INoteDBClient {

    protected final NetHttpTransport netHttpTransport;
    protected final HttpRequestFactory httpRequestFactory;
    protected final JsonFactory jsonFactory;

    protected final String serverLocation;
    protected final Gson gson;

    /**
     * Créer un nouveau client par défaut.
     */
    public AntoninServerDBClient()
    {
        this.netHttpTransport = new NetHttpTransport();
        this.httpRequestFactory = this.netHttpTransport.createRequestFactory();
        this.jsonFactory = new GsonFactory();
        this.serverLocation = "http://51.254.201.22:8500";
        this.gson = new Gson();
    }

    /**
     * Créer et configurer son client perso.
     *
     * @param serverLocation Url d'accès au serveur.
     */
    public AntoninServerDBClient(String serverLocation)
    {
        this.netHttpTransport = new NetHttpTransport();
        this.httpRequestFactory = this.netHttpTransport.createRequestFactory();
        this.jsonFactory = new GsonFactory();
        this.serverLocation = serverLocation;
        this.gson = new Gson();
    }

    /**
     * Retourne une url vers le serveur.
     *
     * @param suffix
     * @return
     */
    protected GenericUrl getUrl(String suffix)
    {
        return new GenericUrl(this.serverLocation + "/" + suffix);
    }

    /**
     * Construit une requête GetAll
     *
     * @param identifier
     * @return
     * @throws IOException
     */
    public HttpRequest makeGetAllRequest(String identifier) throws IOException
    {
        Log.i(getClass().getCanonicalName(), "CREATING POST REQUEST ON /getAll FOR USER <" + identifier + ">");

        Map<String, String> params = new HashMap<String, String>();
        params.put("user", identifier);

        return this.httpRequestFactory.buildPostRequest(
                this.getUrl("getAll"),
                new JsonHttpContent(this.jsonFactory, params)
        );
    }

    /**
     * Parsing simple.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public Map<String, List<String>> doRequest(HttpRequest request) throws IOException
    {
        Log.i(getClass().getCanonicalName(), "EXECUTE REQUEST...");
        HttpResponse response = request.execute();

        if(response.getStatusCode() == 200) {
            Log.i(getClass().getCanonicalName(), "BUILD OBJECT FROM RESPONSE");

            Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
            Map<String, List<String>> result = gson.fromJson(response.parseAsString(), type);

            return result;
        }
        else {
            return null;
        }
    }

    /**
     * Pas de retour.
     *
     * @param request
     * @throws IOException
     */
    public void doAction(HttpRequest request) throws IOException
    {
        Log.i(getClass().getCanonicalName(), "EXECUTE REQUEST...");
        HttpResponse response = request.execute();
    }

    @Override
    public void open() throws DBRequestException
    {

    }

    @Override
    public IUser getUser(UserKey key) throws DBRequestException
    {
        User user = new User(key.name);

        try {
            HttpRequest request = this.makeGetAllRequest(key.name);
            Map<String, List<String>> result = this.doRequest(request);

            if(result != null) {
                for(String categoryName : result.keySet()) {
                    ICategory category = new Category(user, categoryName, result.get(categoryName));
                    user.addCategory(category);
                }
            }
            else {
                Log.i(getClass().getCanonicalName(), "ERROR /getAll FOR USER <" + key + ">");
                user = null;
            }
        } catch (IOException e) {
            throw new DBRequestException(e);
        }

        return user;
    }

    @Override
    public void setUser(IUser user) throws DBRequestException
    {

    }

    @Override
    public void addNote(CategoryKey categoryKey, String note) throws DBRequestException
    {
        try {
            HttpRequest request = this.makeAddNoteRequest(categoryKey, note);
            this.doAction(request);
        } catch (IOException e) {
            throw new DBRequestException(e);
        }
    }

    private HttpRequest makeAddNoteRequest(CategoryKey key, String note) throws IOException
    {
        Log.i(getClass().getCanonicalName(), "CREATING POST REQUEST ON /addToDo FOR NOTE <" + key + " : " + note + ">");

        Map<String, String> params = new HashMap<>();
        params.put("list", key.categoryName);
        params.put("user", key.owner.name);
        params.put("name", note);

        return this.httpRequestFactory.buildPostRequest(
                this.getUrl("addToDo"),
                new JsonHttpContent(this.jsonFactory, params)
        );
    }

    private HttpRequest makeRemoveNoteRequest(CategoryKey key, String note) throws IOException
    {
        Log.i(getClass().getCanonicalName(), "CREATING POST REQUEST ON /removeToDo FOR NOTE <" + key + " : " + note + ">");

        Map<String, String> params = new HashMap<>();
        params.put("list", key.categoryName);
        params.put("user", key.owner.name);
        params.put("name", note);

        return this.httpRequestFactory.buildPostRequest(
                this.getUrl("removeToDo"),
                new JsonHttpContent(this.jsonFactory, params)
        );
    }

    @Override
    public void removeNote(CategoryKey categoryKey, String note) throws DBRequestException
    {
        try {
            HttpRequest request = this.makeRemoveNoteRequest(categoryKey, note);
            this.doAction(request);
        } catch (IOException e) {
            throw new DBRequestException(e);
        }
    }

    protected HttpRequest makeCreateCategoryRequest(CategoryKey key) throws IOException
    {
        Log.i(getClass().getCanonicalName(), "CREATING POST REQUEST ON /addType FOR CATEGORY <" + key + ">");

        Map<String, String> params = new HashMap<>();
        params.put("list", key.categoryName);

        return this.httpRequestFactory.buildPostRequest(
                this.getUrl("addType"),
                new JsonHttpContent(this.jsonFactory, params)
        );
    }

    @Override
    public void createCategory(CategoryKey key) throws DBRequestException
    {
        try {
            HttpRequest request = this.makeCreateCategoryRequest(key);
            this.doAction(request);
        } catch (IOException e) {
            throw new DBRequestException(e);
        }
    }

    @Override
    public void close() throws DBRequestException {

    }
}
