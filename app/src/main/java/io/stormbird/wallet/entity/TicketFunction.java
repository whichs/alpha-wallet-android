package io.stormbird.wallet.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.keplerproject.luajava.LuaState;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rx.Subscription;
import rx.functions.Action1;

class DownloadLink
{
    int totalSize;
    URL fileURL;
}

public class TicketFunction
{
    private static Context ctx;
    private static ImageView targetLayout;
    private static int layoutX;
    private static int layoutY;
    private static OkHttpClient httpClient;
    private static Disposable disposable;
    private static boolean loadedScript;

    private static Map<String, Long> scriptAccess = new HashMap<String, Long>();

    public static void setContext(Context c)
    {
        scriptAccess.clear();
        ctx = c;
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build();
    }

    public static boolean hasLuaScript()
    {
        return loadedScript;
    }

    public static void setXY(int x, int y)
    {
        layoutX = x;
        layoutY = y;
    }

    public static void setTargetLayout(ImageView ll)
    {
        targetLayout = ll;
    }

    public static void loadLuaContract(String address)
    {
        loadedScript = false;
        String luaScript = getContractScript(address);
        if (luaScript != null)
        {
            //load this as our contract
            LuaState lua = Token.getLua();
            if (lua != null)
            {
                loadedScript = true;
                lua.LdoString(luaScript);
            }
        }
    }

    private static String getContractScript(String address)
    {
        try
        {
            String contractScript = loadScript(address);
            if (contractScript != null)
            {
                //check access time
                if (scriptAccess.get(address) < System.currentTimeMillis())
                {
                    return fetchScriptFromServer(address).subscribeOn(Schedulers.io()).blockingSingle();
                }
                else
                {
                    return contractScript;
                }
            }
            else
            {
                return fetchScriptFromServer(address).subscribeOn(Schedulers.io()).blockingSingle();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static String loadScript(String address) throws Exception
    {
        String fName = address + "cScript.lua";
        File f = getLocalFile(fName);
        if (f.exists())
        {
            //restore to string
            return getStringFromFile(f);
        }
        else
        {
            return null;
        }
    }

    private static String getStringFromFile(File luaFile) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        String line = null;
        FileInputStream fin = new FileInputStream(luaFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        fin.close();
        return sb.toString();
    }

    private static Observable<String> fetchScriptFromServer(String address)
    {
        return Observable.fromCallable(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("https://app.awallet.io:80/api/getScript/");
            sb.append(address);
            String result = null;

            try
            {
                Request request = new Request.Builder()
                        .url(sb.toString())
                        .get()
                        .build();

                okhttp3.Response response = httpClient.newCall(request).execute();

                result = response.body().string();
                scriptAccess.put(address, System.currentTimeMillis() + 1000*60*10); //check once per 10 mins
                storeFile(address, result);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return result;
        });
    }

    private static void storeFile(String address, String result) throws IOException
    {
        String fName = address + "cScript.lua";
        File file = getLocalFile(fName);

        FileOutputStream fos = new FileOutputStream(file);
        OutputStream os = new BufferedOutputStream(fos);
        os.write(result.getBytes(), 0, result.length());
        os.close();
        fos.close();
    }

    public static void defineTargetBackground(String f)
    {
        File backgroundFile = getLocalFile(f);
        try
        {
            if (backgroundFile.exists())
            {
                Bitmap bitmap = BitmapFactory.decodeFile(backgroundFile.getAbsolutePath());
                if (bitmap.getHeight() > layoutY || bitmap.getWidth() > layoutX)
                {
                    double scaleXFactor = (double)bitmap.getWidth() / (double)layoutX;
                    double scaleYFactor = (double)bitmap.getHeight() / (double)layoutY;
                    if (scaleXFactor > scaleYFactor)
                    {
                        bitmap = Bitmap.createScaledBitmap(bitmap, layoutX, (int)((double)layoutY/scaleXFactor), false);
                    }
                    else
                    {
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int)((double)bitmap.getWidth()/scaleYFactor), layoutY, false);
                    }
                }

                targetLayout.setImageBitmap(bitmap);// .setBackground(drawable);
            }
        }
        catch (Exception e)
        {
            Log.i("LuaFunctions", f);
            if (backgroundFile == null || !backgroundFile.exists())
            {
                Log.i("LuaFunctions", "File not found error.");
            }
            else
            {
                Log.i("LuaFunctions", "File Exists but is not valid graphic file.");
            }
        }
    }

    public static int getValue(int v)
    {
        Log.i("TicFunJava", "weird search string: " + v);
        return v+1;
    }

    /**
     * This Lua accessible method is structured in this strange way to deal with the requirements:
     * 1. Lua is synchronous and can't accept a callback (hence we can't use react correctly)
     * 2. Android needs to run
     * @param url
     * @return
     */
    public static String loadResource(String url)
    {
        return DownloadFile(url).subscribeOn(Schedulers.io()).toObservable().blockingSingle();
    }

    private static File getLocalFile(String strURL)
    {
        int slashIndex = strURL.lastIndexOf('/');
        String name = strURL.substring(slashIndex + 1);

        //in this case, going to save it in the private data directory of the app
        return new File(ctx.getFilesDir(), name);
    }

    private static Single<String> DownloadFile(String strURL)
    {
        return Single.fromCallable(() -> {
            byte[] largebuffer = new byte[65536];
            String errorFile = "error";
            String returnValue = errorFile;
            File targetFile;
            try
            {
                targetFile = getLocalFile(strURL);
                //already have it locally?
                if (targetFile.exists()) return targetFile.getName();

                DownloadLink dl = obtainFileConnection(strURL);

                if (dl == null) return errorFile;

                FileOutputStream fos = new FileOutputStream(targetFile);

                OutputStream os = new BufferedOutputStream(fos);

                int bufferLength = 0; //used to store a temporary size of the buffer
                InputStream in = new BufferedInputStream(dl.fileURL.openStream());

                int downloadedSize = 0;
                int lastpoint = -1;

                //now, read through the input buffer and write the contents to the file
                while ((bufferLength = in.read(largebuffer)) > 0)
                {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    os.write(largebuffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    //Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize) ;
                    float total = ((float) downloadedSize / (float) dl.totalSize) * 100;
                    int prg = (int) total;
                    if (prg != lastpoint)
                    {
                        lastpoint = prg;
                    }
                }

                returnValue = targetFile.getName();

                os.close();
                fos.close();
                in.close();
            }
            catch (MalformedURLException e)
            {
                returnValue = errorFile;
                e.printStackTrace();
            }
            catch (IOException e)
            {
                returnValue = errorFile;
                e.printStackTrace();
            }
            catch (Exception e)
            {
                returnValue = errorFile;
                e.printStackTrace();
            }

            return returnValue;
        });
    }

    private static DownloadLink obtainFileConnection(String strURL) throws IOException
    {
        URL fileAddr = new URL(strURL);
        //create the new connection
        HttpURLConnection urlConnection = (HttpURLConnection) fileAddr.openConnection();
        //set up some things on the connection
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        //and connect!
        urlConnection.connect();

        int totalSize;
        totalSize = urlConnection.getContentLength();

        if (totalSize <= 0) return null;

        DownloadLink dl = new DownloadLink();
        dl.totalSize = totalSize;
        dl.fileURL = fileAddr;

        return dl;
    }
}
