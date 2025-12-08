<script lang="ts">
  let {
    open = $bindable(false),
    size = "max-w-2xl",
    children
  } = $props<{
    open: boolean;
    size?: string;
    children: import("svelte").Snippet;
  }>();

  let modal: HTMLDialogElement;

  // Sync open prop with modal
  $effect(() => {
    if (open && modal) {
      modal.showModal();
    } else if (!open && modal) {
      modal.close();
    }
  });
</script>

<dialog bind:this={modal} class="modal" onclose={() => (open = false)}>
  <div class="modal-box {size}">
    {@render children()}
  </div>
  <form method="dialog" class="modal-backdrop">
    <button>close</button>
  </form>
</dialog>
