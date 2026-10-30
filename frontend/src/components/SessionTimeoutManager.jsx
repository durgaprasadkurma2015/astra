import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Clock3, X, ShieldCheck } from "lucide-react";
import { useAuth } from "../context/useAuth.js";

const INACTIVITY_TIMEOUT_MS = 15 * 60 * 1000;
const REDIRECT_COUNTDOWN_SECONDS = 10;

export default function SessionTimeoutManager() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [showModal, setShowModal] = useState(false);
  const [countdown, setCountdown] = useState(
    REDIRECT_COUNTDOWN_SECONDS
  );

  const inactivityTimer = useRef(null);
  const countdownTimer = useRef(null);
  const redirecting = useRef(false);
  const showModalRef = useRef(false);
  const userRef = useRef(user);

  // Keep the latest authentication state available to timer callbacks.
useEffect(() => {
  userRef.current = user;
}, [user]);

  const clearInactivityTimer = useCallback(() => {
    if (inactivityTimer.current !== null) {
      clearTimeout(inactivityTimer.current);
      inactivityTimer.current = null;
    }
  }, []);

  const clearCountdownTimer = useCallback(() => {
    if (countdownTimer.current !== null) {
      clearInterval(countdownTimer.current);
      countdownTimer.current = null;
    }
  }, []);

  const clearTimers = useCallback(() => {
    clearInactivityTimer();
    clearCountdownTimer();
  }, [clearInactivityTimer, clearCountdownTimer]);

  const goToLogin = useCallback(() => {
    if (redirecting.current) return;

    redirecting.current = true;
    showModalRef.current = false;

    clearTimers();
    setShowModal(false);

    logout();
    navigate("/login", { replace: true });
  }, [clearTimers, logout, navigate]);

  const startCountdown = useCallback(() => {
    if (redirecting.current || !userRef.current) return;

    // Prevent multiple countdowns from running simultaneously.
    clearTimers();

    showModalRef.current = true;
    setShowModal(true);
    setCountdown(REDIRECT_COUNTDOWN_SECONDS);

    let remaining = REDIRECT_COUNTDOWN_SECONDS;

    countdownTimer.current = setInterval(() => {
      remaining -= 1;
      setCountdown(remaining);

      if (remaining <= 0) {
        goToLogin();
      }
    }, 1000);
  }, [clearTimers, goToLogin]);

  const resetInactivityTimer = useCallback(() => {
    if (
      !userRef.current ||
      showModalRef.current ||
      redirecting.current
    ) {
      return;
    }

    clearInactivityTimer();

    inactivityTimer.current = setTimeout(() => {
      startCountdown();
    }, INACTIVITY_TIMEOUT_MS);
  }, [clearInactivityTimer, startCountdown]);

  const continueSession = useCallback(() => {
    if (redirecting.current) return;

    // Close the warning and stop its countdown.
    showModalRef.current = false;
    clearCountdownTimer();

    setShowModal(false);
    setCountdown(REDIRECT_COUNTDOWN_SECONDS);

    // Start a fresh inactivity period.
    resetInactivityTimer();
  }, [clearCountdownTimer, resetInactivityTimer]);

  // Handle an expired session reported by the API interceptor.
  useEffect(() => {
    const handleExpiredSession = () => {
      startCountdown();
    };

    window.addEventListener(
      "astra:session-expired",
      handleExpiredSession
    );

    return () => {
      window.removeEventListener(
        "astra:session-expired",
        handleExpiredSession
      );
    };
  }, [startCountdown]);

  // Attach user activity listeners and manage the inactivity timer.
  useEffect(() => {
    if (!user) {
      clearTimers();
      showModalRef.current = false;
      redirecting.current = false;

      return undefined;
    }

    const activityEvents = [
      "mousemove",
      "mousedown",
      "keydown",
      "scroll",
      "touchstart",
      "click",
    ];

    const handleActivity = () => {
      resetInactivityTimer();
    };

    activityEvents.forEach((eventName) => {
      window.addEventListener(eventName, handleActivity, {
        passive: true,
      });
    });

    resetInactivityTimer();

    return () => {
      activityEvents.forEach((eventName) => {
        window.removeEventListener(eventName, handleActivity);
      });

      clearTimers();
    };
  }, [user, resetInactivityTimer, clearTimers]);

  // Hide the popup when the user is logged out.
  if (!showModal || !user) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
      <section
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="session-expired-title"
        aria-describedby="session-expired-description"
        className="relative w-full max-w-xl overflow-hidden rounded-3xl border border-violet-400/30 bg-gradient-to-br from-[#141827] to-[#090d19] p-6 text-center text-white shadow-2xl shadow-violet-950/40 sm:p-9"
      >
        <button
          type="button"
          onClick={goToLogin}
          aria-label="Go to login"
          className="absolute right-5 top-5 rounded-lg p-2 text-slate-400 transition hover:bg-white/10 hover:text-white"
        >
          <X size={20} />
        </button>

        <div className="mx-auto flex h-24 w-24 items-center justify-center rounded-full border-2 border-violet-500 bg-violet-500/10 text-violet-300 shadow-lg shadow-violet-600/20">
          <Clock3 size={42} strokeWidth={1.7} />
        </div>

        <h2
          id="session-expired-title"
          className="mt-7 text-2xl font-bold sm:text-3xl"
        >
          Your session has expired
        </h2>

        <p
          id="session-expired-description"
          className="mx-auto mt-4 max-w-md text-sm leading-7 text-slate-300 sm:text-base"
        >
          You have been inactive for a while due to security
          reasons. Please continue your session or log in again.
        </p>

        <div className="mt-7 grid gap-3 sm:grid-cols-2">
          <button
            type="button"
            onClick={continueSession}
            className="rounded-xl border border-slate-400/70 px-5 py-3.5 font-semibold text-white transition hover:bg-white/5"
          >
            Stay on Page
          </button>

          <button
            type="button"
            onClick={goToLogin}
            className="rounded-xl bg-gradient-to-r from-violet-600 to-purple-600 px-5 py-3.5 font-semibold text-white shadow-lg shadow-violet-950/30 transition hover:from-violet-500 hover:to-purple-500"
          >
            Go to Login
          </button>
        </div>

        <p
          className="mt-6 text-sm text-slate-400"
          aria-live="polite"
        >
          Redirecting to login in{" "}
          <span className="font-bold text-violet-300">
            {countdown}
          </span>{" "}
          seconds...
        </p>

        <div className="mt-5 flex items-center justify-center gap-2 text-xs text-slate-500">
          <ShieldCheck size={15} className="text-violet-400" />
          Your account security is important to us.
        </div>
      </section>
    </div>
  );
}