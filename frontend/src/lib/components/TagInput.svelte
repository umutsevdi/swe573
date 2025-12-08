<script lang="ts">
  import { X, Plus } from "@lucide/svelte";

  let {
    tags = $bindable([]),
    placeholder = "Add a tag (press Enter)",
    maxTags
  }: {
    tags: string[];
    placeholder?: string;
    maxTags?: number;
  } = $props();

  let tagInput = $state("");

  function addTag() {
    const trimmed = tagInput.trim().toLowerCase();
    if (
      trimmed &&
      !tags.includes(trimmed) &&
      (!maxTags || tags.length < maxTags)
    ) {
      tags = [...tags, trimmed];
      tagInput = "";
    }
  }

  function removeTag(tagToRemove: string) {
    tags = tags.filter((tag) => tag !== tagToRemove);
  }

  function handleKeydown(event: KeyboardEvent) {
    if (event.key === "Enter") {
      event.preventDefault();
      addTag();
    }
  }
</script>

<div class="form-control">
  <div class="mb-2 flex gap-2">
    <input
      type="text"
      {placeholder}
      class="input-bordered input flex-1"
      bind:value={tagInput}
      onkeydown={handleKeydown}
    />
    <button type="button" class="btn btn-outline" onclick={addTag}>
      <Plus class="h-4 w-4" />
    </button>
  </div>
  {#if tags.length > 0}
    <div class="flex flex-wrap gap-2">
      {#each tags as tag (tag)}
        <div class="badge gap-2 badge-outline badge-accent">
          {tag}
          <button
            type="button"
            class="btn btn-ghost btn-xs"
            onclick={() => removeTag(tag)}
          >
            <X class="h-3 w-3" />
          </button>
        </div>
      {/each}
    </div>
  {/if}
</div>
