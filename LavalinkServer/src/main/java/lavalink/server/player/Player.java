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

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameProvider;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.lavalink.youtube.ManagerFactory;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import io.netty.buffer.ByteBuf;
import lavalink.api.IPlayer;
import lavalink.api.ISocketContext;
import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import lavalink.server.logging.ConsoleLogging;
import lavalink.server.player.filters.FilterChain;
import lavalink.server.config.ServerConfig;
import moe.kyokobot.koe.MediaConnection;
import moe.kyokobot.koe.media.OpusAudioFrameProvider;
import org.json.JSONObject;
import org.yaml.snakeyaml.util.ArrayUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Player extends AudioEventAdapter implements IPlayer {

    private final SocketContext socketContext;
    private final long guildId;
    private final ServerConfig serverConfig;
    private final AudioPlayer player;
    private final AudioPlayerManager playerManager;
    private final AudioLossCounter audioLossCounter = new AudioLossCounter();
    private AudioFrame lastFrame = null;
    private FilterChain filters;
    private ScheduledFuture<?> myFuture = null;
    private boolean endMarkerHit = false;


    public Player(SocketContext socketContext, long guildId, AudioPlayerManager audioPlayerManager, ServerConfig serverConfig) {
        this.socketContext = socketContext;
        this.guildId = guildId;
        this.serverConfig = serverConfig;
        this.player = audioPlayerManager.createPlayer();
        this.player.addListener(this);
        this.player.addListener(new EventEmitter(audioPlayerManager, this));
        this.player.addListener(audioLossCounter);
        this.playerManager = audioPlayerManager;
    }

    public void play(AudioTrack track) {
        player.playTrack(track);
        SocketServer.Companion.sendPlayerUpdate(socketContext, this);
    }

    public void download(AudioTrack track, String savePath) {
        try {
            AudioPlayer downloadPlayer = playerManager.createPlayer();
            downloadPlayer.playTrack(track);
            ConsoleLogging.LogInfo("Downloading " + track.getIdentifier() + " to " + savePath);
            MutableAudioFrame frame = new MutableAudioFrame();
            while (downloadPlayer.getPlayingTrack() != null) {
                AudioFrame prov = downloadPlayer.provide();
                if (prov == null)
                    continue;
                frame.store(prov.getData(), prov.getDataLength(), frame.getDataLength());
            }
            FileOutputStream fos = new FileOutputStream(savePath);
            fos.write(frame.getData());
            fos.close();
            JSONObject obj = new JSONObject();
            obj.put("op", "dl");
            obj.put("identifier", track.getIdentifier());
            socketContext.send(obj);
        }
        catch (Exception e) {
            ConsoleLogging.LogError(e.getMessage());
        }
    }

    public void stop() {
        player.stopTrack();
    }

    public void destroy() {
        player.destroy();
    }

    public void setPause(boolean b) {
        player.setPaused(b);
    }

    @Override
    public AudioPlayer getAudioPlayer() {
        return player;
    }

    @Override
    public AudioTrack getTrack() {
        return player.getPlayingTrack();
    }

    @Override
    public long getGuildId() {
        return guildId;
    }

    @Override
    public ISocketContext getSocketContext() {
        return socketContext;
    }

    public void seekTo(long position) {
        AudioTrack track = player.getPlayingTrack();

        if (track == null) throw new RuntimeException("Can't seek when not playing anything");

        track.setPosition(position);
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public void setEndMarkerHit(boolean hit) {
        this.endMarkerHit = hit;
    }

    public boolean getEndMarkerHit() {
        return this.endMarkerHit;
    }

    public JSONObject getState() {
        JSONObject json = new JSONObject();

        if (player.getPlayingTrack() != null)
            json.put("position", player.getPlayingTrack().getPosition());
        json.put("time", System.currentTimeMillis());

        return json;
    }

    SocketContext getSocket() {
        return socketContext;
    }

    @Nullable
    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public AudioLossCounter getAudioLossCounter() {
        return audioLossCounter;
    }

    private int getInterval() {
        return serverConfig.getPlayerUpdateInterval();
    }

    public boolean isPlaying() {
        return player.getPlayingTrack() != null && !player.isPaused();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        myFuture.cancel(false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (myFuture == null || myFuture.isCancelled()) {
            myFuture = socketContext.getPlayerUpdateService().scheduleAtFixedRate(() -> {
                if (socketContext.getSessionPaused()) return;

                SocketServer.Companion.sendPlayerUpdate(socketContext, this);
            }, 0, this.getInterval(), TimeUnit.SECONDS);
        }
    }

    public void provideTo(MediaConnection connection) {
        connection.setAudioSender(new Provider(connection));
    }

    private class Provider extends OpusAudioFrameProvider {
        public Provider(MediaConnection connection) {
            super(connection);
        }

        @Override
        public boolean canProvide() {
            lastFrame = player.provide();

            if (lastFrame == null) {
                audioLossCounter.onLoss();
                return false;
            }
            else
                return true;

        }

        @Override
        public void retrieveOpusFrame(ByteBuf buf) {
            audioLossCounter.onSuccess();
            buf.writeBytes(lastFrame.getData());
        }
    }

    @Nullable
    public FilterChain getFilters() {
        return filters;
    }

    public void setFilters(FilterChain filters) {
        this.filters = filters;

        if (filters.isEnabled())
            player.setFilterFactory(filters);
        else
            player.setFilterFactory(null);
    }
}
