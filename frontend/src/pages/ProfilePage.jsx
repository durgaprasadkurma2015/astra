import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  UserRound,
  MapPin,
  ShieldCheck,
  ArrowLeft,
  Save,
  Pencil,
  X,
  Mail,
  CalendarDays,
  ShoppingBag,
  ShoppingCart,
  LockKeyhole,
  Eye,
  EyeOff,
  LogOut,
  Package,
  CheckCircle2,
  Sparkles,
  Globe,
} from "lucide-react";

import { useAuth } from "../context/useAuth.js";
import authService from "../services/authService";
import fileService from "../services/fileService";
const emptyProfile = {
  fullName: "",
  email: "",
  dateOfBirth: "",
  gender: "",
  phoneNumber: "",
  alternatePhoneNumber: "",
  addressLine1: "",
  addressLine2: "",
  landmark: "",
  city: "",
  district: "",
  state: "",
  country: "",
  postalCode: "",
  preferredLanguage: "",
  role: "CUSTOMER",
  emailVerified: false,
  createdAt: null,
};

function getPasswordRules(password) {
  return [
    {
      label: "At least 8 characters",
      valid: password.length >= 8 && password.length <= 72,
    },
    {
      label: "One uppercase letter",
      valid: /[A-Z]/.test(password),
    },
    {
      label: "One lowercase letter",
      valid: /[a-z]/.test(password),
    },
    {
      label: "One number",
      valid: /\d/.test(password),
    },
    {
      label: "One special character",
      valid: /[^A-Za-z0-9]/.test(password),
    },
  ];
}

function passwordIsStrong(password) {
  return getPasswordRules(password).every((rule) => rule.valid);
}

const inputClass =
  "mt-2 w-full rounded-xl border border-white/10 bg-[#0a1021] px-4 py-3 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-violet-400/70 focus:ring-2 focus:ring-violet-500/10 disabled:cursor-not-allowed disabled:opacity-70";

function calculateAge(dateOfBirth) {
  if (!dateOfBirth) return "";

  const dob = new Date(`${dateOfBirth}T00:00:00`);

  if (Number.isNaN(dob.getTime())) return "";

  const today = new Date();

  let age = today.getFullYear() - dob.getFullYear();

  if (
    today.getMonth() < dob.getMonth() ||
    (today.getMonth() === dob.getMonth() &&
      today.getDate() < dob.getDate())
  ) {
    age--;
  }

  return age >= 0 ? age : "";
}

function formatDate(value) {
  if (!value) return "Not available";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return "Not available";

  return date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function Field({
  label,
  name,
  value,
  onChange,
  type = "text",
  disabled = false,
  required = false,
  options,
}) {
  return (
    <div className="min-w-0">
      <label
        htmlFor={name}
        className="text-sm font-medium text-slate-300"
      >
        {label}
      </label>

      {options ? (
        <select
          id={name}
          name={name}
          value={value || ""}
          onChange={onChange}
          disabled={disabled}
          required={required}
          className={inputClass}
        >
          <option value="">Select {label.toLowerCase()}</option>

          {options.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      ) : (
        <input
          id={name}
          name={name}
          type={type}
          value={value || ""}
          onChange={onChange}
          disabled={disabled}
          required={required}
          maxLength={
            name === "phoneNumber" ||
            name === "alternatePhoneNumber"
              ? 20
              : undefined
          }
          className={inputClass}
        />
      )}
    </div>
  );
}

function SectionHeading({ icon: Icon, title, description }) {
  return (
    <div className="mb-6 flex items-start gap-3">
      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-violet-400/20 bg-violet-500/10 text-violet-300">
        <Icon size={21} />
      </div>

      <div>
        <h2 className="text-lg font-bold text-white">{title}</h2>

        {description && (
          <p className="mt-1 text-sm leading-5 text-slate-400">
            {description}
          </p>
        )}
      </div>
    </div>
  );
}

function InfoRow({ icon: Icon, label, value }) {
  return (
    <div className="flex min-w-0 items-start gap-3">
      <Icon
        size={17}
        className="mt-0.5 shrink-0 text-violet-300"
      />

      <div className="min-w-0 flex-1">
        <p className="text-xs text-slate-500">{label}</p>

        <p className="mt-1 break-words text-sm text-slate-200">
          {value || "Not provided"}
        </p>
      </div>
    </div>
  );
}

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(emptyProfile);
  const [original, setOriginal] = useState(emptyProfile);

  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");
const [profilePictureUrl, setProfilePictureUrl] = useState("");
const [pictureUploading, setPictureUploading] = useState(false);
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const passwordRules = getPasswordRules(passwordForm.newPassword);

  const passwordStrength = passwordRules.filter(
    (rule) => rule.valid
  ).length;

  const passwordsMatch =
    passwordForm.confirmPassword.length > 0 &&
    passwordForm.newPassword === passwordForm.confirmPassword;

  function handlePasswordChange(event) {
    const { name, value } = event.target;

    setPasswordForm((current) => ({
      ...current,
      [name]: value,
    }));

    setPasswordError("");
    setPasswordMessage("");
  }

  async function handlePasswordSubmit(event) {
    event.preventDefault();

    setPasswordError("");
    setPasswordMessage("");

    if (!passwordForm.currentPassword) {
      setPasswordError("Please enter your current password.");
      return;
    }

    if (!passwordIsStrong(passwordForm.newPassword)) {
      setPasswordError(
        "Use 8–72 characters with uppercase, lowercase, number, and special character."
      );
      return;
    }

    if (
      passwordForm.newPassword !== passwordForm.confirmPassword
    ) {
      setPasswordError(
        "New password and confirmation do not match."
      );
      return;
    }

    if (
      passwordForm.currentPassword === passwordForm.newPassword
    ) {
      setPasswordError(
        "Choose a password different from your current password."
      );
      return;
    }

    try {
      setPasswordLoading(true);

      const response = await authService.changePassword(
        passwordForm.currentPassword,
        passwordForm.newPassword
      );

      setPasswordMessage(
        response.message || "Password changed successfully."
      );

      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });

      setShowCurrentPassword(false);
      setShowNewPassword(false);
      setShowConfirmPassword(false);
    } catch (err) {
      setPasswordError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          (typeof err.response?.data === "string"
            ? err.response.data
            : "") ||
          "Unable to change password. Please check your current password and try again."
      );
    } finally {
      setPasswordLoading(false);
    }
  }
useEffect(() => {
  let cancelled = false;
  let objectUrl = null;

  async function loadPicture() {
    try {
      const files = await fileService.getMyFiles();

      const picture = files.find(
        (file) => file.purpose === "PROFILE_IMAGE"
      );

      if (!picture) return;

      const blob = await fileService.getProfilePictureContent(
        picture.id
      );

      objectUrl = URL.createObjectURL(blob);

      if (!cancelled) {
        setProfilePictureUrl(objectUrl);
      } else {
        URL.revokeObjectURL(objectUrl);
      }
    } catch {
      // Keep the existing initials avatar if no picture is available.
    }
  }

  void loadPicture();

  return () => {
    cancelled = true;
    if (objectUrl) URL.revokeObjectURL(objectUrl);
  };
}, []);

async function handleProfilePictureChange(event) {
  const file = event.target.files?.[0];
  event.target.value = "";

  if (!file) return;

  setError("");
  setMessage("");

  try {
    setPictureUploading(true);

    const uploaded = await fileService.uploadProfilePicture(file);
    const blob = await fileService.getProfilePictureContent(uploaded.id);
    const nextUrl = URL.createObjectURL(blob);

    setProfilePictureUrl((previousUrl) => {
      if (previousUrl) URL.revokeObjectURL(previousUrl);
      return nextUrl;
    });

    setMessage("Profile picture updated successfully.");
  } catch (err) {
    setError(
      err.response?.data?.detail ||
      err.response?.data?.message ||
      "Unable to upload your profile picture."
    );
  } finally {
    setPictureUploading(false);
  }
}
  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      try {
        const data = await authService.getCurrentUser();

        if (!cancelled) {
          const normalized = {
            ...emptyProfile,
            ...data,
            email: data.email || user?.email || "",
            fullName: data.fullName || user?.fullName || "",
            dateOfBirth: data.dateOfBirth || "",
          };

          setProfile(normalized);
          setOriginal(normalized);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.detail ||
              err.response?.data?.message ||
              "Unable to load your profile."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadProfile();

    return () => {
      cancelled = true;
    };
  }, [user?.email, user?.fullName]);

  function handleChange(event) {
    const { name, value } = event.target;

    setProfile((current) => ({
      ...current,
      [name]: value,
    }));
  }

  function startEditing() {
    setEditing(true);
    setError("");
    setMessage("");
  }

  function handleCancel() {
    setProfile(original);
    setEditing(false);
    setError("");
    setMessage("");
  }

  async function handleSave(event) {
    event.preventDefault();

    setError("");
    setMessage("");

    if (!profile.fullName.trim()) {
      setError("Full name is required.");
      return;
    }

    const phonePattern = /^[+]?[0-9 ()-]{7,20}$/;

    if (
      profile.phoneNumber &&
      !phonePattern.test(profile.phoneNumber)
    ) {
      setError("Enter a valid mobile number.");
      return;
    }

    if (
      profile.alternatePhoneNumber &&
      !phonePattern.test(profile.alternatePhoneNumber)
    ) {
      setError("Enter a valid alternate mobile number.");
      return;
    }

    if (
      profile.dateOfBirth &&
      calculateAge(profile.dateOfBirth) === ""
    ) {
      setError("Enter a valid date of birth.");
      return;
    }

    try {
      setSaving(true);

      const payload = {
        fullName: profile.fullName.trim(),
        dateOfBirth: profile.dateOfBirth || null,
        gender: profile.gender,
        phoneNumber: profile.phoneNumber,
        alternatePhoneNumber: profile.alternatePhoneNumber,
        addressLine1: profile.addressLine1,
        addressLine2: profile.addressLine2,
        landmark: profile.landmark,
        city: profile.city,
        district: profile.district,
        state: profile.state,
        country: profile.country,
        postalCode: profile.postalCode,
        preferredLanguage: profile.preferredLanguage,
      };

      const saved = await authService.updateProfile(payload);

      const normalized = {
        ...emptyProfile,
        ...saved,
        email: saved.email || profile.email,
        dateOfBirth: saved.dateOfBirth || "",
      };

      setProfile(normalized);
      setOriginal(normalized);
      setEditing(false);
      setMessage("Your profile has been saved successfully.");
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          "Unable to save your profile. Please try again."
      );
    } finally {
      setSaving(false);
    }
  }

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const age = calculateAge(profile.dateOfBirth);

  const initials = (profile.fullName || user?.fullName || "A")
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");

  if (loading) {
    return (
      <div className="flex min-h-[65vh] items-center justify-center bg-[#050714] text-white">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-2 border-violet-400/20 border-t-violet-400" />

          <p className="mt-4 text-sm text-slate-400">
            Loading your profile...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="overflow-hidden bg-[#050714] text-white">
      {/* Background accents */}
      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute -left-40 top-40 h-96 w-96 rounded-full bg-violet-700/10 blur-[120px]" />
        <div className="absolute -right-40 top-20 h-96 w-96 rounded-full bg-indigo-600/10 blur-[120px]" />
      </div>

      <div className="relative mx-auto max-w-[1440px] px-4 py-6 sm:px-6 lg:px-10 lg:py-9">
        {/* Breadcrumb */}
        <div className="mb-5 flex items-center gap-2 text-xs text-slate-500">
          <Link to="/" className="hover:text-violet-300">
            Home
          </Link>

          <span>/</span>

          <span className="text-slate-300">My Profile</span>
        </div>

        {/* Profile hero */}
        <section className="relative overflow-hidden rounded-3xl border border-violet-400/20 bg-gradient-to-r from-violet-950/90 via-[#10142d] to-indigo-950/80 p-6 shadow-2xl shadow-violet-950/20 sm:p-8">
          <div className="pointer-events-none absolute -right-10 -top-24 h-72 w-72 rounded-full bg-violet-500/20 blur-3xl" />

          <div className="relative flex flex-col gap-6 xl:flex-row xl:items-center xl:justify-between">
            <div className="flex min-w-0 items-center gap-5">
             <div className="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-full border-4 border-violet-300/30 bg-gradient-to-br from-violet-500 to-indigo-700 text-2xl font-black sm:h-24 sm:w-24 sm:text-3xl">
  {profilePictureUrl ? (
    <img
      src={profilePictureUrl}
      alt="Your profile"
      className="h-full w-full object-cover"
    />
  ) : (
    initials || "A"
  )}
</div>

              <div className="min-w-0">
                <p className="flex flex-wrap items-center gap-2 text-xs font-bold uppercase tracking-[0.2em] text-violet-300">
                  <Sparkles size={14} />
                  Your ASTRA account
                </p>

                <h1 className="mt-2 break-words text-2xl font-black sm:text-3xl">
                  {profile.fullName || "My Profile"}
                </h1>

                <p className="mt-2 break-all text-sm text-slate-300">
                  {profile.email || user?.email || "Email not available"}
                </p>

                <div className="mt-3 flex flex-wrap gap-2">
                  <span className="rounded-full border border-violet-300/20 bg-violet-400/10 px-3 py-1 text-xs font-semibold text-violet-200">
                    {profile.role || "CUSTOMER"}
                  </span>

                  <span
                    className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-semibold ${
                      profile.emailVerified
                        ? "border-emerald-400/20 bg-emerald-400/10 text-emerald-300"
                        : "border-amber-400/20 bg-amber-400/10 text-amber-200"
                    }`}
                  >
                    <CheckCircle2 size={13} />

                    {profile.emailVerified
                      ? "Email verified"
                      : "Email not verified"}
                  </span>
                </div>
              </div>
            </div>

            {!editing && (
              <button
                type="button"
                onClick={startEditing}
                className="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-violet-600 to-purple-600 px-5 py-3 text-sm font-bold shadow-lg shadow-violet-950/30 transition hover:from-violet-500 hover:to-purple-500"
              >
                <Pencil size={17} />
                Edit Profile
              </button>
            )}
          </div>
        </section>

        {/* Feedback */}
        {error && (
          <div
            role="alert"
            className="mt-5 rounded-2xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
          >
            {error}
          </div>
        )}

        {message && (
          <div
            role="status"
            className="mt-5 flex items-start gap-2 rounded-2xl border border-emerald-400/20 bg-emerald-400/10 p-4 text-sm text-emerald-200"
          >
            <CheckCircle2 size={18} className="mt-0.5 shrink-0" />
            {message}
          </div>
        )}

        <div className="mt-6 grid items-start gap-6 xl:grid-cols-[minmax(0,1.8fr)_minmax(280px,0.9fr)]">
          {/* Main profile form */}
          <form onSubmit={handleSave} className="min-w-0 space-y-6">
            {/* Personal information */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 shadow-xl shadow-black/10 sm:p-7">
              <SectionHeading
                icon={UserRound}
                title="Personal Information"
                description="Manage your personal details and keep them up to date."
              />

              <div className="grid gap-x-5 gap-y-5 sm:grid-cols-2">
                <Field
                  label="Full name *"
                  name="fullName"
                  value={profile.fullName}
                  onChange={handleChange}
                  disabled={!editing}
                  required
                />

                <Field
                  label="Email address"
                  name="email"
                  value={profile.email}
                  disabled
                />

                <Field
                  label="Date of birth"
                  name="dateOfBirth"
                  type="date"
                  value={profile.dateOfBirth}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <div>
                <div>
  <label className="text-sm font-medium text-slate-300">
    Age
  </label>

  <div className="mt-2 flex min-h-12 items-center rounded-xl border border-white/10 bg-white/[0.025] px-4 text-sm text-slate-300">
    <CalendarDays
      size={16}
      className="mr-2 text-violet-300"
    />

    {age === "" ? "Not provided" : `${age} years`}
  </div>
</div>

<div>
  <label className="text-sm font-medium text-slate-300">
    Profile picture
  </label>

  <div className="mt-2">
    <label
      className={`inline-flex cursor-pointer items-center gap-2 rounded-xl border border-white/10 px-4 py-3 text-sm text-slate-200 transition hover:border-violet-400/50 ${
        pictureUploading
          ? "cursor-not-allowed opacity-60"
          : ""
      }`}
    >
      {pictureUploading ? "Uploading..." : "Change picture"}

      <input
        type="file"
        className="sr-only"
        accept="image/jpeg,image/png,image/webp"
        disabled={pictureUploading}
        onChange={handleProfilePictureChange}
      />
    </label>
  </div>
</div>

                  <div className="mt-2 flex min-h-12 items-center rounded-xl border border-white/10 bg-white/[0.025] px-4 text-sm text-slate-300">
                    <CalendarDays
                      size={16}
                      className="mr-2 text-violet-300"
                    />

                    {age === "" ? "Not provided" : `${age} years`}
                  </div>
                </div>

                <Field
                  label="Gender"
                  name="gender"
                  value={profile.gender}
                  onChange={handleChange}
                  disabled={!editing}
                  options={[
                    "Male",
                    "Female",
                    "Other",
                    "Prefer not to say",
                  ]}
                />

                <Field
                  label="Preferred language"
                  name="preferredLanguage"
                  value={profile.preferredLanguage}
                  onChange={handleChange}
                  disabled={!editing}
                  options={[
                    "English",
                    "Telugu",
                    "Hindi",
                    "Tamil",
                    "Kannada",
                    "Malayalam",
                    "Other",
                  ]}
                />

                <Field
                  label="Mobile number"
                  name="phoneNumber"
                  value={profile.phoneNumber}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <Field
                  label="Alternate mobile (optional)"
                  name="alternatePhoneNumber"
                  value={profile.alternatePhoneNumber}
                  onChange={handleChange}
                  disabled={!editing}
                />
              </div>

              <p className="mt-4 text-xs text-slate-500">
                Include your country calling code when needed, for
                example +91.
              </p>
            </section>

            {/* Address information */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 shadow-xl shadow-black/10 sm:p-7">
              <SectionHeading
                icon={MapPin}
                title="Address Details"
                description="Manage the address information saved to your profile."
              />

              <div className="grid gap-x-5 gap-y-5 sm:grid-cols-2">
                <div className="sm:col-span-2">
                  <Field
                    label="Address line 1"
                    name="addressLine1"
                    value={profile.addressLine1}
                    onChange={handleChange}
                    disabled={!editing}
                  />
                </div>

                <div className="sm:col-span-2">
                  <Field
                    label="Address line 2 (optional)"
                    name="addressLine2"
                    value={profile.addressLine2}
                    onChange={handleChange}
                    disabled={!editing}
                  />
                </div>

                <Field
                  label="Landmark"
                  name="landmark"
                  value={profile.landmark}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <Field
                  label="City / Town"
                  name="city"
                  value={profile.city}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <Field
                  label="District"
                  name="district"
                  value={profile.district}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <Field
                  label="State / Province"
                  name="state"
                  value={profile.state}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <Field
                  label="Country"
                  name="country"
                  value={profile.country}
                  onChange={handleChange}
                  disabled={!editing}
                />

                <Field
                  label="PIN / ZIP code"
                  name="postalCode"
                  value={profile.postalCode}
                  onChange={handleChange}
                  disabled={!editing}
                />
              </div>
            </section>

            {/* Account information */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 shadow-xl shadow-black/10 sm:p-7">
              <SectionHeading
                icon={ShieldCheck}
                title="Account Information"
                description="View your account status and membership details."
              />

              <div className="grid gap-5 sm:grid-cols-2">
                <div>
                  <p className="text-xs text-slate-500">Account role</p>
                  <p className="mt-2 text-sm font-semibold text-slate-200">
                    {profile.role || "CUSTOMER"}
                  </p>
                </div>

                <div>
                  <p className="text-xs text-slate-500">
                    Email verification
                  </p>

                  <p
                    className={`mt-2 text-sm font-semibold ${
                      profile.emailVerified
                        ? "text-emerald-300"
                        : "text-amber-200"
                    }`}
                  >
                    {profile.emailVerified
                      ? "Verified"
                      : "Not verified"}
                  </p>
                </div>

                <div>
                  <p className="text-xs text-slate-500">
                    Member since
                  </p>

                  <p className="mt-2 text-sm font-semibold text-slate-200">
                    {formatDate(profile.createdAt)}
                  </p>
                </div>

                <div>
                  <p className="text-xs text-slate-500">
                    Account status
                  </p>

                  <p className="mt-2 text-sm font-semibold text-slate-200">
                    <span className="mr-2 inline-block h-2 w-2 rounded-full bg-emerald-400" />
                    Account information loaded
                  </p>
                </div>
              </div>
            </section>

            {/* Save / cancel */}
            {editing && (
              <div className="sticky bottom-3 z-20 flex flex-col-reverse gap-3 rounded-2xl border border-white/10 bg-[#0a0e20]/95 p-4 shadow-2xl backdrop-blur-xl sm:flex-row sm:justify-end">
                <button
                  type="button"
                  onClick={handleCancel}
                  disabled={saving}
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/15 px-5 py-3 text-sm font-semibold text-slate-200 transition hover:bg-white/5 disabled:opacity-50"
                >
                  <X size={17} />
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={saving}
                  className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-violet-600 to-purple-600 px-5 py-3 text-sm font-bold transition hover:from-violet-500 hover:to-purple-500 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <Save size={17} />
                  {saving ? "Saving..." : "Save Changes"}
                </button>
              </div>
            )}
          </form>

          {/* Right sidebar */}
          <aside className="space-y-5">
            {/* Profile identity card */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 sm:p-6">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-500/10 text-violet-300">
                  <UserRound size={21} />
                </div>

                <div>
                  <h2 className="font-bold">Your profile</h2>
                  <p className="mt-1 text-xs text-slate-500">
                    Account overview
                  </p>
                </div>
              </div>

              <div className="mt-5 flex items-center gap-4 rounded-xl border border-white/5 bg-white/[0.025] p-4">
          <div className="flex h-14 w-14 shrink-0 items-center justify-center overflow-hidden rounded-full bg-gradient-to-br from-violet-500 to-indigo-700 text-xl font-black">
  {profilePictureUrl ? (
    <img
      src={profilePictureUrl}
      alt="Your profile"
      className="h-full w-full object-cover"
    />
  ) : (
    initials || "A"
  )}
</div>

                <div className="min-w-0">
                  <p className="truncate font-semibold">
                    {profile.fullName || "ASTRA Customer"}
                  </p>

                  <p className="mt-1 break-all text-xs text-slate-400">
                    {profile.email || user?.email || "Email unavailable"}
                  </p>
                </div>
              </div>

         <p className="mt-4 text-xs leading-5 text-slate-500">
  Your profile details and profile picture are managed
  through your ASTRA account.
</p>
            </section>

            {/* Account status */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 sm:p-6">
              <SectionHeading
                icon={ShieldCheck}
                title="Account status"
                description="Your current account details."
              />

              <div className="space-y-5">
                <InfoRow
                  icon={Mail}
                  label="Email verification"
                  value={
                    profile.emailVerified
                      ? "Email verified"
                      : "Not verified"
                  }
                />

                <InfoRow
                  icon={UserRound}
                  label="Account type"
                  value={profile.role || "CUSTOMER"}
                />

                <InfoRow
                  icon={CalendarDays}
                  label="Member since"
                  value={formatDate(profile.createdAt)}
                />

                <InfoRow
                  icon={Globe}
                  label="Preferred language"
                  value={profile.preferredLanguage}
                />
              </div>
            </section>

            {/* Quick actions */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 sm:p-6">
              <SectionHeading
                icon={Sparkles}
                title="Quick actions"
                description="Shortcuts to your store."
              />

              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
                <Link
                  to="/products"
                  className="group flex items-center justify-between gap-3 rounded-xl border border-violet-400/20 bg-violet-500/[0.04] p-4 transition hover:border-violet-400/50 hover:bg-violet-500/10"
                >
                  <span className="flex items-center gap-3">
                    <ShoppingBag
                      size={19}
                      className="text-violet-300"
                    />

                    <span className="text-sm font-semibold">
                      Browse products
                    </span>
                  </span>

                  <ArrowLeft
                    size={16}
                    className="rotate-180 text-slate-500 transition group-hover:text-violet-300"
                  />
                </Link>

                <Link
                  to="/cart"
                  className="group flex items-center justify-between gap-3 rounded-xl border border-white/10 p-4 transition hover:border-violet-400/40 hover:bg-white/[0.03]"
                >
                  <span className="flex items-center gap-3">
                    <ShoppingCart
                      size={19}
                      className="text-violet-300"
                    />

                    <span className="text-sm font-semibold">
                      View cart
                    </span>
                  </span>

                  <ArrowLeft
                    size={16}
                    className="rotate-180 text-slate-500 transition group-hover:text-violet-300"
                  />
                </Link>

                <Link
                  to="/my-orders"
                  className="group flex items-center justify-between gap-3 rounded-xl border border-white/10 p-4 transition hover:border-violet-400/40 hover:bg-white/[0.03]"
                >
                  <span className="flex items-center gap-3">
                    <Package
                      size={19}
                      className="text-violet-300"
                    />

                    <span className="text-sm font-semibold">
                      My Orders
                    </span>
                  </span>

                  <ArrowLeft
                    size={16}
                    className="rotate-180 text-slate-500 transition group-hover:text-violet-300"
                  />
                </Link>
              </div>
            </section>

            {/* Change password */}
            <section className="rounded-2xl border border-white/10 bg-[#0a0e20]/90 p-5 sm:p-6">
              <SectionHeading
                icon={LockKeyhole}
                title="Change password"
                description="Update the password for your ASTRA account."
              />

              {passwordError && (
                <div
                  role="alert"
                  className="mb-4 rounded-xl border border-red-400/20 bg-red-400/10 p-3 text-sm text-red-200"
                >
                  {passwordError}
                </div>
              )}

              {passwordMessage && (
                <div
                  role="status"
                  className="mb-4 rounded-xl border border-emerald-400/20 bg-emerald-400/10 p-3 text-sm text-emerald-200"
                >
                  {passwordMessage}
                </div>
              )}

              <form
                onSubmit={handlePasswordSubmit}
                className="space-y-4"
              >
                {[
                  {
                    id: "currentPassword",
                    name: "currentPassword",
                    label: "Current password",
                    placeholder: "Enter current password",
                    value: passwordForm.currentPassword,
                    visible: showCurrentPassword,
                    toggle: () =>
                      setShowCurrentPassword((value) => !value),
                    autoComplete: "current-password",
                  },
                  {
                    id: "newPassword",
                    name: "newPassword",
                    label: "New password",
                    placeholder: "Enter new password",
                    value: passwordForm.newPassword,
                    visible: showNewPassword,
                    toggle: () =>
                      setShowNewPassword((value) => !value),
                    autoComplete: "new-password",
                  },
                  {
                    id: "confirmPassword",
                    name: "confirmPassword",
                    label: "Confirm new password",
                    placeholder: "Re-enter new password",
                    value: passwordForm.confirmPassword,
                    visible: showConfirmPassword,
                    toggle: () =>
                      setShowConfirmPassword((value) => !value),
                    autoComplete: "new-password",
                  },
                ].map((field) => (
                  <div key={field.id}>
                    <label
                      htmlFor={field.id}
                      className="text-sm font-medium text-slate-300"
                    >
                      {field.label}
                    </label>

                    <div className="relative mt-2">
                      <LockKeyhole
                        size={18}
                        className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                      />

                      <input
                        id={field.id}
                        name={field.name}
                        type={field.visible ? "text" : "password"}
                        autoComplete={field.autoComplete}
                        minLength={
                          field.name === "currentPassword"
                            ? undefined
                            : 8
                        }
                        maxLength={72}
                        value={field.value}
                        onChange={handlePasswordChange}
                        className="w-full rounded-xl border border-white/10 bg-[#0a1021] py-3 pl-11 pr-12 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-violet-400 focus:ring-2 focus:ring-violet-500/20"
                        placeholder={field.placeholder}
                        required
                      />

                      <button
                        type="button"
                        onClick={field.toggle}
                        aria-label={
                          field.visible
                            ? "Hide password"
                            : "Show password"
                        }
                        className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                      >
                        {field.visible ? (
                          <EyeOff size={18} />
                        ) : (
                          <Eye size={18} />
                        )}
                      </button>
                    </div>

                    {/* Password strength and requirements */}
                    {field.name === "newPassword" && (
                      <div className="mt-3" aria-live="polite">
                        <div className="flex gap-1.5">
                          {Array.from({ length: 5 }, (_, index) => (
                            <div
                              key={index}
                              className={`h-1.5 flex-1 rounded-full transition-colors ${
                                index < passwordStrength
                                  ? passwordStrength <= 2
                                    ? "bg-red-500"
                                    : passwordStrength <= 4
                                      ? "bg-amber-400"
                                      : "bg-emerald-500"
                                  : "bg-slate-700"
                              }`}
                            />
                          ))}
                        </div>

                        <p className="mt-2 text-xs text-slate-400">
                          Password strength:{" "}
                          <span
                            className={`font-semibold ${
                              passwordStrength <= 2
                                ? "text-red-400"
                                : passwordStrength <= 4
                                  ? "text-amber-400"
                                  : "text-emerald-400"
                            }`}
                          >
                            {field.value
                              ? passwordStrength <= 2
                                ? "Weak"
                                : passwordStrength <= 4
                                  ? "Medium"
                                  : "Strong"
                              : "Not entered"}
                          </span>
                        </p>

                        <div className="mt-3 grid grid-cols-1 gap-2 rounded-xl bg-slate-800/70 p-3 sm:grid-cols-2">
                          {passwordRules.map((rule) => (
                            <p
                              key={rule.label}
                              className={`flex items-center gap-2 text-xs ${
                                rule.valid
                                  ? "text-emerald-400"
                                  : "text-slate-400"
                              }`}
                            >
                              <span aria-hidden="true">
                                {rule.valid ? "✓" : "○"}
                              </span>

                              {rule.label}
                            </p>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Confirm password match */}
                    {field.name === "confirmPassword" &&
                      field.value.length > 0 && (
                        <p
                          aria-live="polite"
                          className={`mt-2 text-xs ${
                            passwordsMatch
                              ? "text-emerald-400"
                              : "text-red-400"
                          }`}
                        >
                          {passwordsMatch
                            ? "Passwords match"
                            : "Passwords do not match"}
                        </p>
                      )}
                  </div>
                ))}

                <button
                  type="submit"
                  disabled={
                    passwordLoading ||
                    !passwordIsStrong(passwordForm.newPassword) ||
                    !passwordsMatch ||
                    !passwordForm.currentPassword
                  }
                  className="w-full rounded-xl bg-gradient-to-r from-violet-600 to-purple-600 px-4 py-3 text-sm font-bold transition hover:from-violet-500 hover:to-purple-500 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {passwordLoading
                    ? "Updating password..."
                    : "Update password"}
                </button>
              </form>
            </section>

            {/* Sign out */}
            <section className="rounded-2xl border border-red-400/15 bg-red-400/[0.035] p-5">
              <div className="flex items-start gap-3">
                <LockKeyhole
                  size={20}
                  className="mt-0.5 shrink-0 text-red-300"
                />

                <div className="flex-1">
                  <h3 className="font-semibold">Sign out</h3>

                  <p className="mt-1 text-xs leading-5 text-slate-400">
                    Log out of your ASTRA account on this session.
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={handleLogout}
                className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-xl border border-red-400/25 px-4 py-3 text-sm font-semibold text-red-200 transition hover:bg-red-400/10"
              >
                <LogOut size={17} />
                Log out
              </button>
            </section>
          </aside>
        </div>
      </div>
    </div>
  );
}