<script lang="ts">
  import { page } from "$app/stores";
  import { Contact, Handshake, Mails, ScrollText } from "@lucide/svelte";
  import { resolve } from "$app/paths";
  import type { Snippet } from "svelte";
  let { children }: { children: Snippet } = $props();

  function isActive(path: string): boolean {
    if (path === "/admin") {
      return $page.url.pathname === path;
    }
    if (path === "/") return $page.url.pathname === "/";
    return $page.url.pathname.startsWith(path);
  }

  // Load navbar avatar when user logs in
</script>

<div class="drawer-open drawer">
  <input id="my-drawer-4" type="checkbox" class="drawer-toggle" />
  <div class="drawer-content">
    <div class="hero min-h-screen bg-base-200">
      <div class="hero-content w-full max-w-8xl flex-col">
        <div class="w-full">
          {@render children?.()}
        </div>
      </div>
    </div>
  </div>
  <div class="drawer-side drawer-end bg-base-200">
    <label for="my-drawer-4" aria-label="close sidebar" class="drawer-overlay"
    ></label>

    <ul class="menu w-full grow gap-4">
      <li>
        <a
          href={resolve("/admin/")}
          class="flex {isActive('/admin')
            ? 'bg-primary text-primary-content'
            : ''}"
        >
          <Contact />
          User Management
        </a>
      </li>

      <li>
        <a
          href={resolve("/admin/events")}
          class="flex {isActive('/admin/events')
            ? 'bg-primary text-primary-content'
            : ''}"
        >
          <Handshake />
          Offers/Needs
        </a>
      </li>

      <li>
        <a
          href={resolve("/admin/threads")}
          class="flex {isActive('/admin/threads')
            ? 'bg-primary text-primary-content'
            : ''}"
        >
          <Mails />
          Threads
        </a>
      </li>

      <li>
        <a
          href={resolve("/admin/transactions")}
          class="flex {isActive('/admin/transactions')
            ? 'bg-primary text-primary-content'
            : ''}"
        >
          <ScrollText />
          Transactions
        </a>
      </li>
    </ul>
  </div>
</div>
