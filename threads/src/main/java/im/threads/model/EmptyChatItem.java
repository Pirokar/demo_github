package im.threads.model;

/**
 * Created by Vit on 14.07.2017.
 * Объект-пустышка для обработки сообщений, относящихся к чату (что бы они не прошли дальше),
 * но не имеющих действия в МП
 */

public class EmptyChatItem implements ChatItem {
    @Override
    public long getTimeStamp() {
        return 0;
    }
}
