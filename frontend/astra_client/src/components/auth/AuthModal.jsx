import { useState } from "react";
import { X, ArrowLeft, ShieldCheck } from "lucide-react";
import axiosClient from "../../api/axiosClient";
import { useDispatch } from "react-redux";
import { setSession } from "../../store/authSlice";

export default function AuthModal({ open, onClose }) {
  const dispatch = useDispatch();
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", phone: "", password: "", otp: "", token: "" });
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  if (!open) return null;

  const update = e => setForm({ ...form, [e.target.name]: e.target.value });
  const go = next => { setMessage(""); setMode(next); };

  const submit = async e => {
    e.preventDefault(); setMessage(""); setBusy(true);
    try {
      if (mode === "login") {
        const { data } = await axiosClient.post("/auth/login", { email: form.email, password: form.password });
        dispatch(setSession(data)); onClose();
      } else if (mode === "register") {
        const { data } = await axiosClient.post("/auth/register", { name: form.name, email: form.email, password: form.password, phone: form.phone || null });
        setMessage(data.message); setMode("otp");
      } else if (mode === "otp") {
        const { data } = await axiosClient.post("/auth/verify-otp", { email: form.email, otp: form.otp });
        dispatch(setSession(data)); onClose();
      } else if (mode === "sms") {
        const { data } = await axiosClient.post(`/auth/sms/verify-otp?phone=${encodeURIComponent(form.phone)}&otp=${encodeURIComponent(form.otp)}`);
        dispatch(setSession(data)); onClose();
      } else if (mode === "forgot") {
        const { data } = await axiosClient.post("/auth/forgot-password", { email: form.email });
        setMessage(data.message + " For local development, see the Spring Boot console.");
      } else if (mode === "reset") {
        const { data } = await axiosClient.post("/auth/reset-password", { token: form.token, newPassword: form.password });
        setMessage(data.message); setMode("login");
      }
    } catch (error) { setMessage(error.response?.data?.message || "Something went wrong. Please try again."); }
    finally { setBusy(false); }
  };

  const googleLogin = () => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    if (!clientId || !window.google?.accounts?.id) { setMessage("Google sign-in is not configured."); return; }
    window.google.accounts.id.initialize({ client_id: clientId, callback: async ({ credential }) => {
      try { const { data } = await axiosClient.post("/auth/google", { idToken: credential }); dispatch(setSession(data)); onClose(); }
      catch (error) { setMessage(error.response?.data?.message || "Google sign-in failed."); }
    }});
    window.google.accounts.id.prompt();
  };

  const sendSms = async () => {
    if (!form.phone) { setMessage("Enter your mobile number first."); return; }
    setMessage(""); setBusy(true);
    try { const { data } = await axiosClient.post(`/auth/sms/send-otp?phone=${encodeURIComponent(form.phone)}`); setMessage(data.message); setMode("sms"); }
    catch (error) { setMessage(error.response?.data?.message || "Unable to send SMS OTP."); }
    finally { setBusy(false); }
  };

  const title = { login: "Welcome back", register: "Create your Astra account", otp: "Verify your email", forgot: "Reset your password", reset: "Create a new password", sms: "Sign in with SMS" }[mode];
  const action = { login: "Sign In", register: "Create Account", otp: "Verify & Continue", forgot: "Send Reset Instructions", reset: "Reset Password", sms: "Verify SMS OTP" }[mode];

  return <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && onClose()}>
    <div className="auth-modal">
      <button className="modal-close" onClick={onClose}><X size={21} /></button>
      <div className="auth-brand"><span className="logo-mark">A</span> Astra</div>
      <h2>{title}</h2>
      <p className="auth-subtitle">{mode === "login" ? "Sign in to continue shopping." : "Secure, simple and made for you."}</p>
      {mode === "otp" && <div className="otp-note"><ShieldCheck size={19} /> Enter the 6-digit code sent to {form.email}.</div>}
      {mode === "sms" && <div className="otp-note"><ShieldCheck size={19} /> Enter the 6-digit SMS code sent to {form.phone}.</div>}

      <form onSubmit={submit}>
        {mode === "register" && <>
          <label>Full name<input name="name" value={form.name} onChange={update} required placeholder="Your name" /></label>
          <label>Phone number (optional)<input name="phone" value={form.phone} onChange={update} placeholder="+919876543210" /></label>
        </>}
        {!["sms", "otp", "reset"].includes(mode) && <label>Email<input type="email" name="email" value={form.email} onChange={update} required placeholder="you@example.com" /></label>}
        {mode === "sms" && <label>Mobile number<input name="phone" value={form.phone} onChange={update} required placeholder="+919876543210" /></label>}
        {mode === "sms" && <label>SMS verification code<input name="otp" value={form.otp} onChange={update} required inputMode="numeric" maxLength={6} placeholder="123456" /></label>}
        {mode === "otp" && <label>Verification code<input name="otp" value={form.otp} onChange={update} required inputMode="numeric" maxLength={6} placeholder="123456" /></label>}
        {["login", "register"].includes(mode) && <label>Password<input type="password" name="password" value={form.password} onChange={update} required minLength={8} placeholder="At least 8 characters" /></label>}
        {mode === "reset" && <><label>New password<input type="password" name="password" value={form.password} onChange={update} required minLength={8} placeholder="At least 8 characters" /></label><label>Reset token<input name="token" value={form.token} onChange={update} required placeholder="Paste reset token" /></label></>}
        {message && <div className="form-message">{message}</div>}
        <button className="auth-submit" disabled={busy}>{busy ? "Please wait..." : action}</button>
      </form>

      {mode === "login" && <>
        <button type="button" className="auth-submit google-btn" onClick={googleLogin}>Continue with Google</button>
        <button type="button" className="auth-link-button" onClick={() => go("sms")}>Use mobile OTP instead</button>
      </>}
      {mode === "sms" && <button type="button" className="auth-submit" onClick={sendSms} disabled={busy}>Resend SMS OTP</button>}

      <div className="auth-links">
        {mode === "login" && <><button onClick={() => go("forgot")}>Forgot password?</button><button onClick={() => go("register")}>Create your Astra account</button></>}
        {mode === "register" && <button onClick={() => go("login")}><ArrowLeft size={14} /> Back to sign in</button>}
        {mode === "otp" && <button onClick={() => go("login")}><ArrowLeft size={14} /> Back to sign in</button>}
        {mode === "forgot" && <><button onClick={() => go("reset")}>I have a reset token</button><button onClick={() => go("login")}><ArrowLeft size={14} /> Back to sign in</button></>}
        {mode === "reset" && <button onClick={() => go("login")}><ArrowLeft size={14} /> Back to sign in</button>}
        {mode === "sms" && <button onClick={() => go("login")}><ArrowLeft size={14} /> Back to sign in</button>}
      </div>
    </div>
  </div>;
}
