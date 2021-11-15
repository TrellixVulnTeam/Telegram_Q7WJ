package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

public class SendAsMenuView extends View {

    //final RectF rectTmp = new RectF();
    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    final MenuDrawable backDrawable = new MenuDrawable() {
        @Override
        public void invalidateSelf() {
            super.invalidateSelf();
            invalidate();
        }
    };
    boolean expanded;
    float expandProgress;

    StaticLayout menuText;
    boolean isOpened;

    Drawable backgroundDrawable;

    private TLObject defaultPeer;
    private Drawable deleteDrawable;
    private AvatarDrawable avatarDrawable;
    private ImageReceiver imageReceiver;
    private int[] colors = new int[8];
    private RectF rect = new RectF();
    private static Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress;
    private boolean deleting;
    private long lastUpdateTime;

    public SendAsMenuView(Context context, TLObject peer) {
        super(context);
        defaultPeer = peer;
        deleteDrawable = getResources().getDrawable(R.drawable.delete);

        ImageLocation imageLocation;
        Object imageParent;

        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setTextSize(AndroidUtilities.dp(12));

        avatarDrawable.setInfo(defaultPeer);

        imageLocation = ImageLocation.getForUserOrChat(defaultPeer, ImageLocation.TYPE_SMALL);
        imageParent = defaultPeer;

        imageReceiver = new ImageReceiver();
        imageReceiver.setRoundRadius(AndroidUtilities.dp(16));
        imageReceiver.setParentView(this);
        imageReceiver.setImageCoords(0, 0, AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        imageReceiver.setImage(imageLocation, "50_50", avatarDrawable, 0, null, imageParent, 1);

        updateColors();
    }

    private void updateColors() {
        int color = avatarDrawable.getColor();
        int back = Theme.getColor(Theme.key_groupcreate_spanBackground);
        colors[0] = Color.red(back);
        colors[1] = Color.red(color);
        colors[2] = Color.green(back);
        colors[3] = Color.green(color);
        colors[4] = Color.blue(back);
        colors[5] = Color.blue(color);
        colors[6] = Color.alpha(back);
        colors[7] = Color.alpha(color);

        paint.setColor(Theme.getColor(Theme.key_chat_messagePanelVoiceBackground));
        int textColor = Theme.getColor(Theme.key_chat_messagePanelVoicePressed);
        backDrawable.setBackColor(textColor);
        backDrawable.setIconColor(textColor);
        deleteDrawable.setColorFilter(new PorterDuffColorFilter(textColor, PorterDuff.Mode.MULTIPLY));
        textPaint.setColor(textColor);
    }

    public boolean isDeleting() {
        return deleting;
    }

    public void startDeleteAnimation() {
        if (deleting) {
            return;
        }
        deleting = true;
        lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    public void cancelDeleteAnimation() {
        if (!deleting) {
            return;
        }
        deleting = false;
        lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    int lastSize;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        int width = AndroidUtilities.dp(30);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(30), MeasureSpec.EXACTLY));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (deleting && progress != 1.0f || !deleting && progress != 0.0f) {
            long newTime = System.currentTimeMillis();
            long dt = newTime - lastUpdateTime;
            if (dt < 0 || dt > 17) {
                dt = 17;
            }
            if (deleting) {
                progress += dt / 120.0f;
                if (progress >= 1.0f) {
                    progress = 1.0f;
                }
            } else {
                progress -= dt / 120.0f;
                if (progress < 0.0f) {
                    progress = 0.0f;
                }
            }
            invalidate();
        }
        canvas.save();
        imageReceiver.draw(canvas);

        if (progress != 0) {
            int color = Theme.getColor(Theme.key_chat_messagePanelVoiceBackground);
            float alpha = Color.alpha(color) / 255.0f;
            backPaint.setColor(color);
            backPaint.setAlpha((int) (255 * progress * alpha));
            canvas.drawCircle(AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp((float) 15.00000001), backPaint);
            canvas.save();
            canvas.rotate(45 * (1.0f - progress), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
            deleteDrawable.setBounds(AndroidUtilities.dp(9), AndroidUtilities.dp(9), AndroidUtilities.dp(21), AndroidUtilities.dp(21));
            deleteDrawable.setAlpha((int) (255 * progress));
            deleteDrawable.draw(canvas);
            canvas.restore();
        }

        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || backgroundDrawable == who;
    }
}
