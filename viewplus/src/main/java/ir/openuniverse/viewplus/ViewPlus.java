package ir.openuniverse.viewplus;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.view.View.NO_ID;

public interface ViewPlus {
	default OnClickListener correctOnClickListener(View view, TypedArray typedArray, int clickablePropertyStyleable,
																								 int onClickPropertyStyleable) {
		if (typedArray.getBoolean(clickablePropertyStyleable, false)) {
			final String onClick = typedArray.getString(onClickPropertyStyleable);//log("onClick="+onClick);
			if (onClick != null) {
				final DeclaredOnClickListener onClickListener = new DeclaredOnClickListener(view, onClick, getActivity(view.getContext()));
				view.setOnClickListener(onClickListener);
				return onClickListener;
			}
		}
		return null;
	}
	
	default Activity getActivity(Context context) {
		while (context instanceof ContextWrapper) {
			if (context instanceof Activity) return (Activity) context;
			context = ((ContextWrapper) context).getBaseContext();
		}
		return null;
	}
	
	class DeclaredOnClickListener implements OnClickListener { // Copied: {@link android.view.View#DeclaredOnClickListener DeclaredOnClickListener}
		private final Activity activity;
		private final View mHostView;
		private final String mMethodName;
		
		private Method mResolvedMethod;
		private Context mResolvedContext;
		
		DeclaredOnClickListener(@NonNull View hostView, @NonNull String methodName, Activity activity) {
			this.activity = activity;
			mHostView = hostView;
			mMethodName = methodName;
		}
		
		@Override public void onClick(@NonNull View v) {
			if (mResolvedMethod == null) resolveMethod(activity); //mHostView.getContext()
			
			try {
				mResolvedMethod.invoke(mResolvedContext, v);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
					  "Could not execute non-public method for android:onClick", e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException(
					  "Could not execute method for android:onClick", e);
			}
		}
		
		private void resolveMethod(@Nullable Context context) {
			while (context != null) {
				try {
					if (!context.isRestricted()) {
						final Method method = context.getClass().getMethod(mMethodName, View.class);
						if (method != null) {
							mResolvedMethod = method;
							mResolvedContext = context;
							return;
						}
					}
				} catch (NoSuchMethodException e) {
					// Failed to find method, keep searching up the hierarchy.
				}
				
				if (context instanceof ContextWrapper) {
					context = ((ContextWrapper) context).getBaseContext();
				} else {
					// Can't search up the hierarchy, null out and fail.
					context = null;
				}
			}
			
			final int id = mHostView.getId();
			final String idText = id == NO_ID ? "" : " with id '"
				  + mHostView.getContext().getResources().getResourceEntryName(id) + "'";
			throw new IllegalStateException("Could not find method " + mMethodName
				  + "(View) in a parent or ancestor Context for android:onClick "
				  + "attribute defined on view " + mHostView.getClass() + idText);
		}
	}
}
