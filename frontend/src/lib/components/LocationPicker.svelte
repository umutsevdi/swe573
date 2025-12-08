<script lang="ts">
  import { browser } from "$app/environment";
  import { MapPin } from "@lucide/svelte";
  import { toast } from "$lib/stores/toast";
  import { API } from "$lib/api";

  interface Props {
    initialLat?: number;
    initialLon?: number;
    initialName?: string;
    height?: string;
    onLocationChange?: (location: {
      lat: number;
      lon: number;
      name: string;
    }) => void;
  }

  let {
    initialLat,
    initialLon,
    initialName,
    height = "h-48",
    onLocationChange
  }: Props = $props();

  let mapContainer: HTMLElement;
  let map: any = null;
  let marker: any = null;
  let selectedLat = $state(initialLat ?? 41.0082); // Default to Istanbul
  let selectedLon = $state(initialLon ?? 28.9784);
  let locationName = $state(initialName || "");

  // Initialize map when component mounts
  $effect(() => {
    if (browser && mapContainer && !map) {
      import("leaflet").then(async (L) => {
        await import("leaflet/dist/leaflet.css");

        // Fix for default markers in Leaflet
        delete (L.Icon.Default.prototype as any)._getIconUrl;
        L.Icon.Default.mergeOptions({
          iconRetinaUrl:
            "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
          iconUrl:
            "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
          shadowUrl:
            "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png"
        });

        // Initialize map
        map = L.map(mapContainer).setView([selectedLat, selectedLon], 13);

        // Add Carto tiles based on theme
        const theme =
          document.documentElement.getAttribute("data-theme") || "light";
        const tileUrl =
          theme === "dark"
            ? "https://cartodb-basemaps-{s}.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png"
            : "https://cartodb-basemaps-{s}.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png";
        L.tileLayer(tileUrl, {
          attribution:
            '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, © <a href="https://carto.com/attributions">CartoDB</a>',
          maxZoom: 19
        }).addTo(map);

        // Add draggable marker
        marker = L.marker([selectedLat, selectedLon], {
          draggable: true
        }).addTo(map);

        // Handle marker drag
        marker.on("dragend", async (event: any) => {
          const position = event.target.getLatLng();
          selectedLat = position.lat;
          selectedLon = position.lng;

          // Reverse geocode to get location name
          try {
            const data = await API.maps.reverse(selectedLat, selectedLon);
            if (data && data.display_name) {
              locationName = data.display_name.split(",")[0]; // Get city name
            }
          } catch (error) {
            console.log("Could not get location name:", error);
            locationName = `${selectedLat.toFixed(4)}, ${selectedLon.toFixed(4)}`;
          }
        });

        // Handle map click
        map.on("click", async (event: any) => {
          const { lat, lng } = event.latlng;
          selectedLat = lat;
          selectedLon = lng;

          // Move marker to clicked location
          marker.setLatLng([lat, lng]);

          // Reverse geocode to get location name
          try {
            const data = await API.maps.reverse(selectedLat, selectedLon);
            if (data && data.display_name) {
              locationName = data.display_name.split(",")[0]; // Get city name
            }
          } catch (error) {
            console.log("Could not get location name:", error);
            locationName = `${selectedLat.toFixed(4)}, ${selectedLon.toFixed(4)}`;
          }
        });

        // Initial reverse geocoding
        try {
          const data = await API.maps.reverse(selectedLat, selectedLon);
          if (data && data.display_name) {
            locationName = data.display_name.split(",")[0];
          }
        } catch (error) {
          console.log("Could not get location name:", error);
          toast.error(
            error instanceof Error
              ? error.message
              : "Failed to get location name"
          );
          locationName = `${selectedLat.toFixed(4)}, ${selectedLon.toFixed(4)}`;
        }
      });
    }

    return () => {
      if (map) {
        map.remove();
        map = null;
      }
    };
  });

  // Update marker position when initial coordinates change
  $effect(() => {
    if (map && marker && initialLat !== undefined && initialLon !== undefined) {
      selectedLat = initialLat;
      selectedLon = initialLon;
      marker.setLatLng([selectedLat, selectedLon]);
      map.setView([selectedLat, selectedLon], 13);

      // Reverse geocode the initial location
      reverseGeocode(selectedLat, selectedLon);
    }
  });

  // Notify parent when location changes
  $effect(() => {
    if (
      onLocationChange &&
      selectedLat !== undefined &&
      selectedLon !== undefined
    ) {
      onLocationChange({
        lat: selectedLat,
        lon: selectedLon,
        name:
          locationName || `${selectedLat.toFixed(4)}, ${selectedLon.toFixed(4)}`
      });
    }
  });

  // Function to reverse geocode coordinates
  async function reverseGeocode(lat: number, lng: number) {
    try {
      const data = await API.maps.reverse(lat, lng);
      if (data && data.display_name) {
        locationName = data.display_name.split(",")[0];
      }
    } catch (error) {
      console.log("Could not get location name:", error);
      toast.error(
        error instanceof Error ? error.message : "Failed to get location name"
      );
      locationName = `${selectedLat.toFixed(4)}, ${selectedLon.toFixed(4)}`;
    }
  }
</script>

<div
  class="{height} overflow-hidden rounded-lg border border-base-300"
  bind:this={mapContainer}
>
  {#if !browser}
    <div class="flex h-full items-center justify-center bg-base-200">
      <MapPin class="h-8 w-8 text-base-content/50" />
    </div>
  {/if}
</div>

{#if locationName}
  <p class="mt-2 text-sm text-base-content/70">
    Selected: <strong>{locationName}</strong> ({selectedLat.toFixed(4)}, {selectedLon.toFixed(
      4
    )})
  </p>
{/if}

<style>
  /* Ensure Leaflet controls are visible on dark themes */
  :global(.leaflet-control-container .leaflet-control) {
    background-color: rgba(255, 255, 255, 0.9);
    color: #333;
  }

  :global(.leaflet-popup-content-wrapper) {
    background-color: rgba(255, 255, 255, 0.95);
    color: #333;
    border-radius: 8px;
  }

  :global(.leaflet-popup-tip) {
    background-color: rgba(255, 255, 255, 0.95);
  }
</style>
