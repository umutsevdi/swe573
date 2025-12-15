<script lang="ts">
  import "../app.css";
  import { resolve } from "$app/paths";
  import EventModal from "$lib/components/EventModal.svelte";
  import ThreadModal from "$lib/components/ThreadModal.svelte";
  import Toast from "$lib/components/Toast.svelte";
  import favicon from "$lib/assets/favicon.svg";
  import {
    BookOpen,
    Github,
    Plus,
    LogOut,
    Settings,
    PanelRightOpen,
    MapPinned,
    Mails,
    Bell,
    MapPinPlus,
    MailPlus,
    Handshake,
    MessageCircle,
    GlobeLock
  } from "@lucide/svelte";
  import { auth } from "$lib/stores/user";
  import { toast } from "$lib/stores/toast";
  import { breadcrumbs } from "$lib/stores/breadcrumbs";
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { HIVE_API } from "$lib/config";
  import { API } from "$lib/api";

  let { children } = $props();

  let createPostModal: unknown;
  let modalOpen = $state(false);
  let threadModalOpen = $state(false);
  let navbarAvatarUrl = $state<string | null>(null);
  let unreadNotifications = $state(0);

  let title = $derived(
    $auth.isLoggedIn
      ? `${$breadcrumbs[$breadcrumbs.length - 1].text} | Hive`
      : "Hive"
  );

  function isActive(path: string): boolean {
    if (path === "/") return $page.url.pathname === "/";
    return $page.url.pathname.startsWith(path);
  }

  function openCreatePostModal() {
    (createPostModal as any)?.openModal();
  }

  async function handleCreateThreadSubmit(
    event: CustomEvent<{ title: string; description: string; tags: string[] }>
  ) {
    const { title, description, tags } = event.detail;

    try {
      const result = await API.threads.create(HIVE_API, $auth.token!, {
        title: title.trim(),
        description: description.trim(),
        tags
      });
      toast.success("Thread created successfully!");

      // Navigate to the new thread page
      goto(resolve(`/forum/${result.id}`));
      threadModalOpen = false;
    } catch (error) {
      toast.error(`Failed to create thread. Please try again. ${error}`);
    }
  }

  async function handlePostSubmit(event: CustomEvent) {
    try {
      const result = await API.events.create(
        HIVE_API,
        $auth.token!,
        event.detail
      );

      toast.success("Event created successfully!");
      // Navigate to the new event page
      goto(resolve(`/event/${result.id}`));
    } catch (error) {
      toast.error(`Failed to create event. ${error}`);
    }
  }

  async function loadNavbarAvatar() {
    if (!$auth.user?.id || !$auth.token) return;
    try {
      navbarAvatarUrl = await API.assets.getProfile(
        HIVE_API,
        $auth.token,
        $auth.user.id,
        "/assets/profile.png"
      );
    } catch (error) {
      const err = error as Error;
      if (err.message.includes("401") || err.message.includes("Unauthorized")) {
        auth.logout();
        goto(resolve("/auth"));
      } else if (
        err.message.includes("404") ||
        err.message.includes("Not Found")
      ) {
        navbarAvatarUrl = "/assets/profile.png";
      } else {
        console.error("Failed to load navbar avatar:", err);
      }
    }
  }

  async function loadUnreadNotifications() {
    if (!$auth.token) return;
    try {
      unreadNotifications = await API.inbox.count(HIVE_API, $auth.token);
    } catch (error) {
      unreadNotifications = 0;
    }
  }

  function handleLogout() {
    auth.logout();
    goto(resolve("/auth"));
  }

  // Load navbar avatar and unread notifications when user logs in
  $effect(() => {
    if ($auth.isLoggedIn && $auth.user?.id && $auth.token) {
      loadNavbarAvatar();
      loadUnreadNotifications();
    }

    // Cleanup blob URL on unmount (only if it's a blob URL)
    return () => {
      if (navbarAvatarUrl && navbarAvatarUrl.startsWith("blob:")) {
        URL.revokeObjectURL(navbarAvatarUrl);
      }
    };
  });
</script>

<EventModal
  mode="create"
  bind:this={createPostModal}
  bind:open={modalOpen}
  on:submit={handlePostSubmit}
/>

<ThreadModal
  mode="create"
  bind:open={threadModalOpen}
  on:submit={handleCreateThreadSubmit}
/>

<div class="drawer-open drawer">
  <input id="my-drawer-4" type="checkbox" class="drawer-toggle" />
  <div class="drawer-content">
    {#if $auth.isLoggedIn}
      <div class="flex items-center">
        <div class="breadcrumbs ml-4 gap-1">
          <ul>
            {#each $breadcrumbs as crumb, i}
              <li>
                {#if crumb.href}
                  <a
                    class="btn text-xl no-underline btn-ghost"
                    href={crumb.href}>{crumb.text}</a
                  >
                {:else if i === $breadcrumbs.length - 1}
                  <a
                    class="btn text-xl no-underline btn-ghost"
                    href={$page.url.pathname}>{crumb.text}</a
                  >
                {:else}
                  <p class="btn text-xl no-underline btn-ghost">
                    {crumb.text}
                  </p>
                {/if}
              </li>
            {/each}
          </ul>
        </div>
      </div>
    {/if}
    {@render children?.()}
    <Toast />
    <footer
      class="footer items-center bg-neutral p-4 text-neutral-content sm:footer-horizontal"
    >
      <aside class="grid-flow-col items-center">
        <p>Copyright © {new Date().getFullYear()} - All right reserved</p>
      </aside>

      <nav
        class="grid-flow-col justify-center gap-4 md:place-self-center md:justify-self-end"
      >
        <ul tabindex="-1" class="menu menu-vertical lg:menu-horizontal">
          <li>
            <a
              class="tooltip tooltip-left"
              href="https://github.com/umutsevdi/swe573/wiki"
              target="_blank"
              data-tip="Application documentation."
            >
              <BookOpen />
            </a>
          </li>
          <li>
            <a
              class="tooltip tooltip-left"
              href="https://github.com/umutsevdi/swe573"
              target="_blank"
              data-tip="Source code is available at GitHub."
            >
              <Github />
            </a>
          </li>
        </ul>
      </nav>
    </footer>

    <!-- Speed Dial FAB for Events and Threads -->
    {#if $auth.isLoggedIn}
      <div class="fab fixed right-8 bottom-8 z-20 fab-flower">
        <!-- Trigger button -->
        <div tabindex="0" role="button" class="btn btn-circle btn-lg btn-info">
          <Plus />
        </div>
        <div class="tooltip tooltip-left" data-tip="Close">
          <div class="fab-close">
            <span class="btn btn-circle btn-lg btn-error">✕</span>
          </div>
        </div>

        <div class="tooltip tooltip-left" data-tip="Create a new Offer or Need">
          <button
            class="fab-main-action btn btn-circle btn-lg btn-primary"
            onclick={openCreatePostModal}
          >
            <MapPinPlus />
          </button>
        </div>
        <div></div>
        <div class="tooltip tooltip-left" data-tip="Create new thread">
          <button
            class="btn btn-circle btn-lg btn-secondary"
            onclick={() => (threadModalOpen = true)}
          >
            <MailPlus />
          </button>
        </div>
      </div>
    {/if}
  </div>

  <div class="drawer-side drawer-end is-drawer-close:overflow-visible">
    <label for="my-drawer-4" aria-label="close sidebar" class="drawer-overlay"
    ></label>
    {#if $auth.isLoggedIn}
      <div
        class="bg-base-400 flex min-h-full flex-col items-start is-drawer-close:w-14 is-drawer-open:w-32"
      >
        <!-- Logo -->
        <div class="mt-4 mb-4 flex w-full justify-center">
          <a
            class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
            data-tip="Hive"
            href="/"><Handshake size="32" /></a
          >
        </div>
        <!-- Sidebar content here -->
        <ul class="menu w-full grow gap-4">
          <li>
            <a
              href={resolve("/")}
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                '/'
              )
                ? 'bg-primary text-primary-content'
                : ''}"
              data-tip="Discover"
              ><MapPinned class="h-6 w-6" />
              <span class="is-drawer-close:hidden">Discover</span>
            </a>
          </li>
          <li>
            <a
              href={resolve("/chat")}
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                '/chat'
              )
                ? 'bg-primary text-primary-content'
                : ''}"
              data-tip="Chat"
              ><MessageCircle class="h-6 w-6" />
              <span class="is-drawer-close:hidden">Chat</span>
            </a>
          </li>
          <li>
            <a
              href={resolve("/forum")}
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                '/forum'
              )
                ? 'bg-primary text-primary-content'
                : ''}"
              data-tip="Forum"
              ><Mails class="h-6 w-6" />
              <span class="is-drawer-close:hidden">Forum</span>
            </a>
          </li>
          <li>
            <a
              href={resolve("/inbox")}
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                '/inbox'
              )
                ? 'bg-primary text-primary-content'
                : ''}"
              data-tip="Inbox"
              ><Bell class="h-6 w-6" />
              <span class="is-drawer-close:hidden">Inbox</span>
              {#if unreadNotifications > 0}
                <span class="badge badge-primary badge-sm"
                  >{unreadNotifications}</span
                >
              {/if}
            </a>
          </li>
          <li>
            <a
              href={resolve(`/user/${$auth.user?.id}`)}
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                '/user'
              )
                ? 'bg-primary text-primary-content'
                : ''}"
              data-tip="Profile"
            >
              <div class="avatar">
                <div
                  class="w-6 rounded-full ring-2 ring-primary ring-offset-2 ring-offset-base-100"
                >
                  <img alt="Profile" src={navbarAvatarUrl} />
                </div>
              </div>
              <span class="is-drawer-close:hidden">Profile</span>
            </a>
          </li>
          {#if $auth.user!.role === "ADMIN" || $auth.user!.role === "OWNER"}
            <li>
              <a
                href={resolve("/admin")}
                class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                  '/admin'
                )
                  ? 'bg-primary text-primary-content'
                  : ''}"
                data-tip="Administration"
                ><GlobeLock class="h-6 w-6" />
                <span class="is-drawer-close:hidden">Administration</span>
              </a>
            </li>
          {/if}
          <li>
            <a
              href={resolve("/inbox")}
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right {isActive(
                '/inbox'
              )
                ? 'bg-primary text-primary-content'
                : ''}"
              data-tip="Settings"
              ><Settings class="h-6 w-6" />
              <span class="is-drawer-close:hidden">Settings</span>
            </a>
          </li>

          <li>
            <button
              class="is-drawer-close:tooltip is-drawer-close:tooltip-right"
              data-tip="Logout"
              onclick={handleLogout}
            >
              <LogOut class="h-5 w-6" />
              <span class="is-drawer-close:hidden">Logout</span>
            </button>
          </li>
        </ul>

        <ul>
          <!-- button to open/close drawer -->
          <div
            class="m-2 is-drawer-close:tooltip is-drawer-close:tooltip-right"
            data-tip="Open"
          >
            <label
              for="my-drawer-4"
              class="drawer-button btn btn-circle btn-ghost is-drawer-open:rotate-y-180"
            >
              <PanelRightOpen />
            </label>
          </div>
        </ul>
      </div>
    {/if}
  </div>
</div>
<svelte:head>
  <title>{title}</title>
  <link rel="icon" href={favicon} />
</svelte:head>
