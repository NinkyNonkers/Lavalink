/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import jakarta.servlet.http.HttpServletRequest;
import lavalink.server.logging.ConsoleLogging;
import lavalink.server.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

@RestController
public class AudioLoaderRestHandler {

    private final AudioPlayerManager audioPlayerManager;

    public AudioLoaderRestHandler(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
    }

    private void log(HttpServletRequest request) {
        String path = request.getServletPath();
        ConsoleLogging.LogInfo("GET " + path);
    }

    private JSONObject trackToJSON(AudioTrack audioTrack) {
        AudioTrackInfo trackInfo = audioTrack.getInfo();

        return new JSONObject()
                .put("title", trackInfo.title)
                .put("author", trackInfo.author)
                .put("length", trackInfo.length)
                .put("identifier", trackInfo.identifier)
                .put("uri", trackInfo.uri)
                .put("isStream", trackInfo.isStream)
                .put("isSeekable", audioTrack.isSeekable())
                .put("position", audioTrack.getPosition())
                .put("sourceName", audioTrack.getSourceManager() == null ? null : audioTrack.getSourceManager().getSourceName());
    }

    private JSONObject encodeLoadResult(LoadResult result) {
        JSONObject json = new JSONObject();
        JSONObject playlist = new JSONObject();
        JSONArray tracks = new JSONArray();

        result.tracks.forEach(track -> {
            JSONObject object = new JSONObject();
            object.put("info", trackToJSON(track));

            try {
                String encoded = Util.toMessage(audioPlayerManager, track);
                object.put("track", encoded);
                tracks.put(object);
            } catch (IOException e) {
                ConsoleLogging.LogError("Failed to encode a track {}, skipping" + track.getIdentifier() + e);
            }
        });

        playlist.put("name", result.playlistName);
        playlist.put("selectedTrack", result.selectedTrack);

        json.put("playlistInfo", playlist);
        json.put("loadType", result.loadResultType);
        json.put("tracks", tracks);

        if (result.loadResultType == ResultStatus.LOAD_FAILED && result.exception != null) {
            JSONObject exception = new JSONObject();
            exception.put("message", result.exception.getLocalizedMessage());
            exception.put("severity", result.exception.severity.toString());

            json.put("exception", exception);
            ConsoleLogging.LogError("Track loading failed " + result.exception);
        }

        return json;
    }

    @GetMapping(value = "/loadtracks", produces = "application/json")
    @ResponseBody
    public CompletionStage<ResponseEntity<String>> getLoadTracks(
            HttpServletRequest request,
            @RequestParam String identifier) {
        return new AudioLoader(audioPlayerManager).load(identifier)
                .thenApply(this::encodeLoadResult)
                .thenApply(loadResultJson -> new ResponseEntity<>(loadResultJson.toString(), HttpStatus.OK));
    }

    @GetMapping(value = "/decodetrack", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getDecodeTrack(HttpServletRequest request, @RequestParam String track)
            throws IOException {

        log(request);

        AudioTrack audioTrack = Util.toAudioTrack(audioPlayerManager, track);

        return new ResponseEntity<>(trackToJSON(audioTrack).toString(), HttpStatus.OK);
    }

    @PostMapping(value = "/decodetracks", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> postDecodeTracks(HttpServletRequest request, @RequestBody String body)
            throws IOException {

        log(request);

        JSONArray requestJSON = new JSONArray(body);
        JSONArray responseJSON = new JSONArray();

        for (int i = 0; i < requestJSON.length(); i++) {
            String track = requestJSON.getString(i);
            AudioTrack audioTrack = Util.toAudioTrack(audioPlayerManager, track);

            JSONObject infoJSON = trackToJSON(audioTrack);
            JSONObject trackJSON = new JSONObject()
                    .put("track", track)
                    .put("info", infoJSON);

            responseJSON.put(trackJSON);
        }

        return new ResponseEntity<>(responseJSON.toString(), HttpStatus.OK);
    }
}
