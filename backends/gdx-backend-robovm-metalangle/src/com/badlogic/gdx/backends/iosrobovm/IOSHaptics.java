/*DO NOT EDIT THIS FILE - it is machine generated*/

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.corehaptic.CHHapticEngine;
import org.robovm.apple.corehaptic.CHHapticEventParameterID;
import org.robovm.apple.corehaptic.CHHapticEventType;
import org.robovm.apple.corehaptic.CHHapticPattern;
import org.robovm.apple.corehaptic.CHHapticPatternDict;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSProcessInfo;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIImpactFeedbackGenerator;
import org.robovm.apple.uikit.UIImpactFeedbackStyle;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.objc.block.VoidBlock1;

/** DO NOT EDIT THIS FILE - it is machine generated */
public class IOSHaptics {

	private CHHapticEngine hapticEngine;

	private boolean hapticsSupport;

	private final boolean vibratorSupport;

	public IOSHaptics (boolean useHaptics) {
		vibratorSupport = useHaptics && UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Phone;
		if (NSProcessInfo.getSharedProcessInfo().getOperatingSystemVersion().getMajorVersion() >= 13) {
			hapticsSupport = useHaptics && CHHapticEngine.capabilitiesForHardware().supportsHaptics();
			if (hapticsSupport) {
				try {
					hapticEngine = new CHHapticEngine();
				} catch (NSErrorException e) {
					Gdx.app.error("IOSHaptics", "Error creating CHHapticEngine. Haptics will be disabled. " + e);
					hapticsSupport = false;
				}
				hapticEngine.setPlaysHapticsOnly(true);
				hapticEngine.setAutoShutdownEnabled(true);
				// The reset handler provides an opportunity to restart the engine.
				hapticEngine.setResetHandler(new Runnable() {

					@Override
					public void run () {
						// Try restarting the engine.
						hapticEngine.start(new VoidBlock1<NSError>() {

							@Override
							public void invoke (NSError nsError) {
								Gdx.app.error("IOSHaptics", "Error restarting CHHapticEngine. Haptics will be disabled.");
								hapticsSupport = false;
							}
						});
					}
				});
			}
		}
	}

	public void vibrate (int milliseconds, boolean fallback) {
		if (hapticsSupport) {
			CHHapticPatternDict hapticDict = getChHapticPatternDict(milliseconds, 0.5f);
			try {
				CHHapticPattern pattern = new CHHapticPattern(hapticDict);
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine.createPlayer(pattern).start(0, ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error starting haptics player. Error code: " + ptr.get().getErrorCode());
				}
			} catch (NSErrorException e) {
				Gdx.app.error("IOSHaptics", "Error creating haptics pattern or player. " + e.getMessage());
			}
		} else if (fallback) {
			AudioServices.playSystemSound(4095);
		}
	}

	public void vibrate (int milliseconds, int amplitude, boolean fallback) {
		if (hapticsSupport) {
			float intensity = MathUtils.clamp(amplitude / 255f, 0, 1);
			CHHapticPatternDict hapticDict = getChHapticPatternDict(milliseconds, intensity);
			try {
				CHHapticPattern pattern = new CHHapticPattern(hapticDict);
				NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
				hapticEngine.createPlayer(pattern).start(0, ptr);
				if (ptr.get() != null) {
					Gdx.app.error("IOSHaptics", "Error starting haptics pattern.");
				}
			} catch (NSErrorException e) {
				Gdx.app.error("IOSHaptics", "Error creating haptics player. " + e.getMessage());
			}
		} else {
			vibrate(milliseconds, fallback);
		}
	}

	private CHHapticPatternDict getChHapticPatternDict (int milliseconds, float intensity) {
		return new CHHapticPatternDict().setPattern(new NSArray<NSObject>(new CHHapticPatternDict()
			.setEvent(new CHHapticPatternDict().setEventType(CHHapticEventType.HapticContinuous).setTime(0.0)
				.setEventDuration(milliseconds / 1000f)
				.setEventParameters(new NSArray<NSObject>(new CHHapticPatternDict()
					.setParameterID(CHHapticEventParameterID.HapticIntensity).setParameterValue(intensity).getDictionary())))
			.getDictionary()));
	}

	public void vibrate (Input.VibrationType vibrationType) {
		if (hapticsSupport) {
			UIImpactFeedbackStyle uiImpactFeedbackStyle;
			switch (vibrationType) {
			case LIGHT:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Soft;
				break;
			case MEDIUM:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Medium;
				break;
			case HEAVY:
				uiImpactFeedbackStyle = UIImpactFeedbackStyle.Heavy;
				break;
			default:
				throw new IllegalArgumentException("Unknown VibrationType " + vibrationType);
			}
			UIImpactFeedbackGenerator uiImpactFeedbackGenerator = new UIImpactFeedbackGenerator(uiImpactFeedbackStyle);
			uiImpactFeedbackGenerator.impactOccurred();
		}
	}

	public boolean isHapticsSupported () {
		return hapticsSupport;
	}

	public boolean isVibratorSupported () {
		return vibratorSupport;
	}
}
