
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Bell,
  CheckCheck,
  Package,
  CreditCard,
  XCircle,
  Truck,
  LoaderCircle,
} from "lucide-react";

import { useAuth } from "../context/useAuth";
import { notificationService } from "../services/notificationService";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  "http://localhost:8080/api/v1";

function getAccessToken() {
  return (
    localStorage.getItem("astra_access_token") ||
    sessionStorage.getItem("astra_access_token")
  );
}

function NotificationIcon({ type }) {
  const icons = {
    ORDER_PLACED: Package,
    ORDER_CANCELLED: XCircle,
    PAYMENT_COMPLETED: CreditCard,
    ORDER_STATUS_CHANGED: Truck,
  };

  const Icon = icons[type] || Bell;

  return <Icon size={19} />;
}

export default function NotificationBell() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const loadNotifications = useCallback(async () => {
    if (!user) return;

    try {
      const [items, count] = await Promise.all([
        notificationService.getNotifications(),
        notificationService.getUnreadCount(),
      ]);

      setNotifications(items);
      setUnreadCount(count);
    } catch (error) {
      console.error("Unable to load notifications:", error);
    }
  }, [user]);

  // Real-time SSE connection.
  useEffect(() => {
    if (!user) return;

    const controller = new AbortController();

    async function connect() {
      while (!controller.signal.aborted) {
        try {
          const token = getAccessToken();

          if (!token) break;

          const response = await fetch(
            `${API_BASE_URL.replace(/\/$/, "")}/notifications/stream`,
            {
              method: "GET",
              headers: {
                Authorization: `Bearer ${token}`,
                Accept: "text/event-stream",
              },
              credentials: "include",
              signal: controller.signal,
            }
          );

          if (!response.ok || !response.body) {
            throw new Error(
              `Notification stream failed: ${response.status}`
            );
          }

          // Load existing notifications after successful connection.
          await loadNotifications();

          const reader = response.body.getReader();
          const decoder = new TextDecoder();

          let buffer = "";

          try {
            while (!controller.signal.aborted) {
              const { value, done } = await reader.read();

              if (done) break;

              buffer += decoder.decode(value, {
                stream: true,
              });

              const blocks = buffer.split(/\r?\n\r?\n/);
              buffer = blocks.pop() || "";

              for (const block of blocks) {
                const eventType = block.match(
                  /^event:\s*(.+)$/m
                )?.[1];

                if (eventType === "notification") {
                  await loadNotifications();
                }
              }
            }
          } finally {
            reader.releaseLock();
          }
        } catch (error) {
          if (controller.signal.aborted) break;

          console.error(
            "Notification connection interrupted:",
            error
          );
        }

        if (!controller.signal.aborted) {
          await new Promise((resolve) =>
            setTimeout(resolve, 3000)
          );
        }
      }
    }

    connect();

    return () => controller.abort();
  }, [user, loadNotifications]);

  const handleMarkRead = async (notification) => {
    try {
      if (!notification.read) {
        await notificationService.markAsRead(notification.id);

        setNotifications((previous) =>
          previous.map((item) =>
            item.id === notification.id
              ? { ...item, read: true }
              : item
          )
        );

        setUnreadCount((count) => Math.max(0, count - 1));
      }

      setOpen(false);

      navigate(notification.targetUrl || "/my-orders");
    } catch (error) {
      console.error(
        "Unable to mark notification as read:",
        error
      );
    }
  };

  const handleMarkAllRead = async () => {
    setLoading(true);

    try {
      await notificationService.markAllAsRead();

      setNotifications((previous) =>
        previous.map((item) => ({
          ...item,
          read: true,
        }))
      );

      setUnreadCount(0);
    } catch (error) {
      console.error(
        "Unable to mark all notifications as read:",
        error
      );
    } finally {
      setLoading(false);
    }
  };

  if (!user) return null;

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-label={`Notifications, ${unreadCount} unread`}
        aria-expanded={open}
        className="relative rounded-xl p-2.5 text-slate-300 transition hover:bg-violet-500/10 hover:text-violet-300"
      >
        <Bell size={21} />

        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white ring-2 ring-[#080b1b]">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-12 z-50 w-[min(360px,calc(100vw-24px))] overflow-hidden rounded-2xl border border-white/10 bg-[#10152b] shadow-2xl">
          <div className="flex items-center justify-between border-b border-white/10 p-4">
            <div>
              <h3 className="font-semibold text-white">
                Notifications
              </h3>

              <p className="text-xs text-slate-400">
                {unreadCount} unread messages
              </p>
            </div>

            <div className="flex items-center gap-2">
              {unreadCount > 0 && (
                <button
                  onClick={handleMarkAllRead}
                  disabled={loading}
                  title="Mark all as read"
                  className="rounded-lg p-2 text-violet-300 hover:bg-white/10"
                >
                  {loading ? (
                    <LoaderCircle
                      size={17}
                      className="animate-spin"
                    />
                  ) : (
                    <CheckCheck size={17} />
                  )}
                </button>
              )}

              <button
                onClick={() => setOpen(false)}
                className="rounded-lg p-2 text-slate-400 hover:bg-white/10"
                aria-label="Close notifications"
              >
                <XCircle size={17} />
              </button>
            </div>
          </div>

          <div className="max-h-[400px] overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="p-8 text-center">
                <Bell
                  size={32}
                  className="mx-auto mb-3 text-slate-500"
                />

                <p className="text-sm text-slate-300">
                  No notifications yet
                </p>

                <p className="mt-1 text-xs text-slate-500">
                  Your order updates will appear here.
                </p>
              </div>
            ) : (
              notifications.map((notification) => (
                <button
                  key={notification.id}
                  onClick={() => handleMarkRead(notification)}
                  className={`flex w-full gap-3 border-b border-white/[0.06] p-4 text-left transition hover:bg-white/[0.06] ${
                    notification.read
                      ? "opacity-70"
                      : "bg-violet-500/[0.07]"
                  }`}
                >
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-violet-500/15 text-violet-300">
                    <NotificationIcon type={notification.type} />
                  </div>

                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-sm font-semibold text-white">
                        {notification.title}
                      </p>

                      {!notification.read && (
                        <span className="h-2 w-2 shrink-0 rounded-full bg-violet-400" />
                      )}
                    </div>

                    <p className="mt-1 text-xs leading-5 text-slate-400">
                      {notification.message}
                    </p>

                    <p className="mt-2 text-[10px] text-slate-500">
                      {notification.createdAt
                        ? new Date(
                            notification.createdAt
                          ).toLocaleString()
                        : ""}
                    </p>
                  </div>
                </button>
              ))
            )}
          </div>

          <div className="border-t border-white/10 p-3">
            <button
              onClick={() => {
                setOpen(false);
                navigate("/my-orders");
              }}
              className="w-full rounded-xl bg-violet-600/15 px-4 py-2 text-sm font-semibold text-violet-300 transition hover:bg-violet-600/25"
            >
              View my orders
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
