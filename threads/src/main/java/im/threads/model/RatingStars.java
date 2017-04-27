package im.threads.model;

/**
 * Created by Admin on 27.04.2017.
 */

public class RatingStars implements ChatItem {
    private Long id;
    private long date;
    private int rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }
}
