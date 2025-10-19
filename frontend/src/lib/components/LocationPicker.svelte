<script lang="ts">
	import { browser } from '$app/environment';
	import { MapPin } from '@lucide/svelte';

	interface Props {
		initialLat?: number;
		initialLng?: number;
		height?: string;
		onLocationChange?: (location: { lat: number; lng: number; name: string }) => void;
	}

	let { initialLat, initialLng, height = 'h-48', onLocationChange }: Props = $props();

	let mapContainer: HTMLElement;
	let map: any = null;
	let marker: any = null;
	let selectedLat = $state(initialLat ?? 41.0082); // Default to Istanbul
	let selectedLng = $state(initialLng ?? 28.9784);
	let locationName = $state('');

	// Initialize map when component mounts
	$effect(() => {
		if (browser && mapContainer && !map) {
			import('leaflet').then(async (L) => {
				await import('leaflet/dist/leaflet.css');

				// Fix for default markers in Leaflet
				delete (L.Icon.Default.prototype as any)._getIconUrl;
				L.Icon.Default.mergeOptions({
					iconRetinaUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
					iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
					shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
				});

				// Initialize map
				map = L.map(mapContainer).setView([selectedLat, selectedLng], 13);

				// Add OpenStreetMap tiles
				L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
					attribution:
						'© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
					maxZoom: 19
				}).addTo(map);

				// Add draggable marker
				marker = L.marker([selectedLat, selectedLng], { draggable: true }).addTo(map);

				// Handle marker drag
				marker.on('dragend', async (event: any) => {
					const position = event.target.getLatLng();
					selectedLat = position.lat;
					selectedLng = position.lng;

					// Reverse geocode to get location name
					try {
						const response = await fetch(
							`https://nominatim.openstreetmap.org/reverse?format=json&lat=${selectedLat}&lon=${selectedLng}&zoom=10&addressdetails=1`
						);
						const data = await response.json();
						if (data && data.display_name) {
							locationName = data.display_name.split(',')[0]; // Get city name
						}
					} catch (error) {
						console.log('Could not get location name:', error);
						locationName = `${selectedLat.toFixed(4)}, ${selectedLng.toFixed(4)}`;
					}

					if (onLocationChange) {
						onLocationChange({
							lat: selectedLat,
							lng: selectedLng,
							name: locationName
						});
					}
				});

				// Handle map click
				map.on('click', async (event: any) => {
					const { lat, lng } = event.latlng;
					selectedLat = lat;
					selectedLng = lng;

					// Move marker to clicked location
					marker.setLatLng([lat, lng]);

					// Reverse geocode to get location name
					try {
						const response = await fetch(
							`https://nominatim.openstreetmap.org/reverse?format=json&lat=${selectedLat}&lon=${selectedLng}&zoom=10&addressdetails=1`
						);
						const data = await response.json();
						if (data && data.display_name) {
							locationName = data.display_name.split(',')[0]; // Get city name
						}
					} catch (error) {
						console.log('Could not get location name:', error);
						locationName = `${selectedLat.toFixed(4)}, ${selectedLng.toFixed(4)}`;
					}

					if (onLocationChange) {
						onLocationChange({
							lat: selectedLat,
							lng: selectedLng,
							name: locationName
						});
					}
				});

				// Initial reverse geocoding
				try {
					const response = await fetch(
						`https://nominatim.openstreetmap.org/reverse?format=json&lat=${selectedLat}&lon=${selectedLng}&zoom=10&addressdetails=1`
					);
					const data = await response.json();
					if (data && data.display_name) {
						locationName = data.display_name.split(',')[0];
					}
				} catch (error) {
					console.log('Could not get initial location name:', error);
					locationName = `${selectedLat.toFixed(4)}, ${selectedLng.toFixed(4)}`;
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
		if (map && marker && initialLat !== undefined && initialLng !== undefined) {
			selectedLat = initialLat;
			selectedLng = initialLng;
			marker.setLatLng([selectedLat, selectedLng]);
			map.setView([selectedLat, selectedLng], 13);

			// Reverse geocode the initial location
			reverseGeocode(selectedLat, selectedLng);
		}
	});

	// Function to reverse geocode coordinates
	async function reverseGeocode(lat: number, lng: number) {
		try {
			const response = await fetch(
				`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=10&addressdetails=1`
			);
			const data = await response.json();
			if (data && data.display_name) {
				locationName = data.display_name.split(',')[0];
			}
		} catch (error) {
			console.log('Could not get location name:', error);
			locationName = `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
		}
	}
</script>

<div class="{height} overflow-hidden rounded-lg border border-base-300" bind:this={mapContainer}>
	{#if !browser}
		<div class="flex h-full items-center justify-center bg-base-200">
			<MapPin class="h-8 w-8 text-base-content/50" />
		</div>
	{/if}
</div>

{#if locationName}
	<p class="mt-2 text-sm text-base-content/70">
		Selected: <strong>{locationName}</strong> ({selectedLat.toFixed(4)}, {selectedLng.toFixed(4)})
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
