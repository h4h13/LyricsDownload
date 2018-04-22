package code.name.monkey.companion.lyrics;

import android.annotation.SuppressLint;
import android.os.Handler;
import code.name.monkey.companion.App;
import code.name.monkey.companion.mvp.model.Song;
import code.name.monkey.companion.rest.KogouClient;
import code.name.monkey.companion.rest.model.KuGouRawLyric;
import code.name.monkey.companion.rest.model.KuGouSearchLyricResult;
import code.name.monkey.companion.util.LyricUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;

/**
 * @author Hemanth S (h4h13).
 */

public class KogouLyricsFetcher {

  private KogouClient mKogouClient;
  private Song mSong;
  private KogouLyricsCallback mCallback;

  public KogouLyricsFetcher(KogouLyricsCallback callback) {
    mCallback = callback;
    mKogouClient = new KogouClient(App.Companion.getContext());
  }

  @SuppressLint("CheckResult")
  public void loadLyrics(final Song song, String duration) {
    mSong = song;
    mKogouClient.getApiService()
        .searchLyric(mSong.getTitle(), duration)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .flatMap(new Function<KuGouSearchLyricResult, ObservableSource<KuGouRawLyric>>() {
          @Override
          public ObservableSource<KuGouRawLyric> apply(
              KuGouSearchLyricResult kuGouSearchLyricResult) {
            if (kuGouSearchLyricResult.status == 200
                && kuGouSearchLyricResult.candidates != null
                && kuGouSearchLyricResult.candidates.size() != 0) {
              KuGouSearchLyricResult.Candidates candidates = kuGouSearchLyricResult.candidates
                  .get(0);
              return mKogouClient.getApiService().getRawLyric(candidates.id, candidates.accesskey);
            } else {
              return Observable.just(null);
            }
          }
        }).map(new Function<KuGouRawLyric, File>() {
      @Override
      public File apply(KuGouRawLyric kuGouRawLyric) {
        if (kuGouRawLyric == null) {
          return null;
        }
        String rawLyric = LyricUtil.INSTANCE.decryptBASE64(kuGouRawLyric.content);
        return LyricUtil.INSTANCE.writeLrcToLoc(song.getTitle(), song.getArtistName(), rawLyric);
      }
    }).subscribe(new Consumer<File>() {
      @Override
      public void accept(File file) {
        if (file == null) {
          return;
        }
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            mCallback.onLyrics(LyricUtil.INSTANCE
                .getLocalLyricFile(mSong.getTitle(), mSong.getArtistName()));
          }
        }, 1);
      }
    }, new Consumer<Throwable>() {
      @Override
      public void accept(Throwable throwable) throws Exception {
        mCallback.onNoLyrics();
      }
    });
        /*.subscribe(new Consumer<KuGouSearchLyricResult>() {
          @Override
          public void accept(KuGouSearchLyricResult kuGouSearchLyricResult) throws Exception {
            parseKugouResult(kuGouSearchLyricResult);
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            mCallback.onNoLyrics();
          }
        });*/
  }

  private void parseKugouResult(KuGouSearchLyricResult kuGouSearchLyricResult) {
    if (kuGouSearchLyricResult != null && kuGouSearchLyricResult.status == 200 &
        kuGouSearchLyricResult.candidates != null &&
        kuGouSearchLyricResult.candidates.size() != 0) {
      KuGouSearchLyricResult.Candidates candidates = kuGouSearchLyricResult.candidates.get(0);
      loadLyricsFile(candidates);
    } else {
      mCallback.onNoLyrics();
    }
  }

  @SuppressLint("CheckResult")
  private void loadLyricsFile(KuGouSearchLyricResult.Candidates candidates) {
    mKogouClient.getApiService().getRawLyric(candidates.id, candidates.accesskey)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<KuGouRawLyric>() {
          @Override
          public void accept(KuGouRawLyric kuGouRawLyric) throws Exception {
            if (kuGouRawLyric == null) {
              mCallback.onNoLyrics();
              return;
            }
            String rawLyric = LyricUtil.INSTANCE.decryptBASE64(kuGouRawLyric.content);
            LyricUtil.INSTANCE.writeLrcToLoc(mSong.getTitle(), mSong.getArtistName(), rawLyric);
            new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                mCallback.onLyrics(LyricUtil.INSTANCE
                    .getLocalLyricFile(mSong.getTitle(), mSong.getArtistName()));
              }
            }, 1);
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            mCallback.onNoLyrics();
          }
        });
  }

  public interface KogouLyricsCallback {

    void onNoLyrics();

    void onLyrics(File file);
  }
}
