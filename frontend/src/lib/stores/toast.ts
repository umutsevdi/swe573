import { writable } from "svelte/store";

export interface Toast {
  id: string;
  message: string;
  type: "success" | "error" | "warning" | "info";
  duration?: number;
}

function createToastStore() {
  const { subscribe, update } = writable<Toast[]>([]);

  let toastId = 0;

  function addToast(
    message: string,
    type: Toast["type"] = "info",
    duration = 4000
  ) {
    const id = (++toastId).toString();
    const toast: Toast = { id, message, type, duration };

    update((toasts) => [...toasts, toast]);

    // Auto remove after duration
    setTimeout(() => {
      removeToast(id);
    }, duration);

    return id;
  }

  function removeToast(id: string) {
    update((toasts) => toasts.filter((t) => t.id !== id));
  }

  function success(message: string, duration?: number) {
    return addToast(message, "success", duration);
  }

  function error(message: string, duration?: number) {
    console.error(message);
    return addToast(message, "error", duration);
  }

  function warning(message: string, duration?: number) {
    return addToast(message, "warning", duration);
  }

  function info(message: string, duration?: number) {
    return addToast(message, "info", duration);
  }

  return {
    subscribe,
    add: addToast,
    remove: removeToast,
    success,
    error,
    warning,
    info
  };
}

export const toast = createToastStore();
