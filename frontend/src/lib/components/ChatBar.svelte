<script lang="ts">
  import { MessageCircle, X } from "@lucide/svelte";
  import { auth } from "$lib/stores/user";

  let { toggleCallback } = $props<{ toggleCallback?: () => boolean }>();

  let isOpen = $state(false);
  let activeTab: "friends" | "groups" | "events" = $state("friends");

  function toggle() {
    if (!toggleCallback || toggleCallback()) {
      isOpen = !isOpen;
    }
  }

  // Mock data for chats
  const chats = {
    friends: [
      {
        id: 1,
        name: "Alice",
        lastMessage: "Hey, how are you?",
        time: "2m ago"
      },
      { id: 2, name: "Bob", lastMessage: "See you tomorrow!", time: "1h ago" }
    ],
    groups: [
      {
        id: 3,
        name: "Project Team",
        lastMessage: "Meeting at 3pm",
        time: "30m ago"
      },
      {
        id: 4,
        name: "Study Group",
        lastMessage: "Homework due Friday",
        time: "2h ago"
      }
    ],
    events: [
      {
        id: 5,
        name: "Event Chat 1",
        lastMessage: "Event details updated",
        time: "1d ago"
      }
    ]
  };
</script>

<div class="fixed right-0 bottom-0 left-0 z-10 bg-primary p-1">
  <div class="flex justify-center">
    <button class="btn btn-circle text-white btn-ghost btn-sm" onclick={toggle}>
      {#if isOpen}
        <X size={18} />
      {:else}
        <MessageCircle size={18} />
      {/if}
    </button>
  </div>

  {#if isOpen}
    <div
      class="absolute right-4 bottom-full left-4 max-h-96 overflow-hidden rounded-t-lg border border-base-300 bg-base-100 shadow-lg"
    >
      <div class="tabs-boxed tabs justify-center p-2">
        <button
          class="tab {activeTab === 'friends' ? 'tab-active' : ''}"
          onclick={() => (activeTab = "friends")}
        >
          Friends
        </button>
        <button
          class="tab {activeTab === 'groups' ? 'tab-active' : ''}"
          onclick={() => (activeTab = "groups")}
        >
          Groups
        </button>
        <button
          class="tab {activeTab === 'events' ? 'tab-active' : ''}"
          onclick={() => (activeTab = "events")}
        >
          Events
        </button>
      </div>

      <div class="max-h-80 overflow-y-auto p-4">
        {#each chats[activeTab] as chat}
          <div
            class="chat-start mb-4 chat cursor-pointer rounded p-2 hover:bg-base-200"
          >
            <div class="chat-bubble">
              <div class="font-semibold">{chat.name}</div>
              <div class="text-sm opacity-75">{chat.lastMessage}</div>
              <div class="mt-1 text-xs opacity-50">{chat.time}</div>
            </div>
          </div>
        {/each}
      </div>
    </div>
  {/if}
</div>
