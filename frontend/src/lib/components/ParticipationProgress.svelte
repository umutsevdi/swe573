<script lang="ts">
  import { CircleCheck, TriangleAlert, Info, Handshake } from "@lucide/svelte";

  type StepType = {
    color: string;
    message?: string;
    icon?: typeof CircleCheck;
  };

  let {
    currentStepIndex,
    currentStatus
  }: {
    currentStepIndex: number;
    currentStatus: string | undefined;
  } = $props();

  let steps = $state([
    { label: "Request Sent", status: "SENT" },
    { label: "Negotiating", status: "NEGOTIATING" },
    { label: "Decision Made", status: "DECIDED" },
    { label: "Completed", status: "COMPLETED" }
  ]);

  // Update step label based on status
  $effect(() => {
    steps[2].label =
      currentStatus === "ACCEPTED"
        ? "Accepted"
        : currentStatus === "REJECTED"
          ? "Rejected"
          : "Decision Made";
  });

  let stepClass = $derived((): StepType => {
    if (!currentStatus) return { color: "primary" };
    switch (currentStatus) {
      case "COMPLETED":
        return { color: "success" };
      case "ACCEPTED":
        return {
          color: "success",
          icon: CircleCheck,
          message: "Negotiation was succesful. See you there."
        };
      case "REJECTED":
        return {
          color: "error",
          message: "You were rejeceted by the owner.",
          icon: TriangleAlert
        };
      case "SENT":
        return {
          color: "accent",
          message: "Waiting for approval.",
          icon: Info
        };
      case "NEGOTIATING":
        return { color: "info", message: "Negotiating.", icon: Handshake };
      default:
        return { color: "primary" };
    }
  });

  let Icon = $derived(stepClass().icon);
</script>

<div class="container mx-auto px-6 py-8">
  <div class="card">
    <div class="card-body">
      <ul class="steps steps-vertical lg:steps-horizontal">
        {#each steps as step, i (step.status)}
          <li
            class="step {i <= currentStepIndex
              ? 'step-' + stepClass().color
              : ''}"
          >
            {step.label}
          </li>
        {/each}
      </ul>
      {#if Icon}
        <div
          role="alert"
          class="mt-4 alert alert-soft {'alert-' + stepClass().color}"
        >
          <Icon class="h-6 w-6 shrink-0 stroke-current" />
          <span>{stepClass().message}</span>
        </div>
      {/if}
    </div>
  </div>
</div>
