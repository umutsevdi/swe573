<script lang="ts">
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';

	interface Event {
		id: string;
		title: string;
		location: string;
		latitude: number;
		longitude: number;
		type: 'Offer' | 'Need';
		tags: string[];
	}

	interface Props {
		events: Event[];
		userLatitude?: number;
		userLongitude?: number;
	}

	let { events, userLatitude, userLongitude }: Props = $props();

	let mapContainer: HTMLElement;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	let map: any = null;

	// Only initialize on client side
	$effect(() => {
		if (browser && mapContainer && !map && events.length > 0) {
			import('leaflet').then((L) => {
				import('leaflet/dist/leaflet.css');

				// Fix for default markers in Leaflet
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				delete (L.Icon.Default.prototype as any)._getIconUrl;
				L.Icon.Default.mergeOptions({
					iconRetinaUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
					iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
					shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
				});

				// Initialize map centered on events
				const bounds: [number, number][] = events.map((event) => [event.latitude, event.longitude]);
				map = L.map(mapContainer).fitBounds(bounds as L.LatLngBoundsExpression, {
					padding: [20, 20]
				});

				// Add OpenStreetMap tiles
				L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
					attribution:
						'© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
					maxZoom: 19
				}).addTo(map);

				// Add markers for each event
				events.forEach((event) => {
					const icon =
						event.type === 'Offer'
							? new L.Icon({
									iconUrl:
										'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
									shadowUrl:
										'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
									iconSize: [25, 41],
									iconAnchor: [12, 41],
									popupAnchor: [1, -34],
									shadowSize: [41, 41]
								})
							: new L.Icon({
									iconUrl:
										'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
									shadowUrl:
										'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
									iconSize: [25, 41],
									iconAnchor: [12, 41],
									popupAnchor: [1, -34],
									shadowSize: [41, 41]
								});

					const marker = L.marker([event.latitude, event.longitude], { icon }).addTo(map);

					const popupContent = `
						<div class="p-2">
							<h3 class="font-bold text-sm">${event.title}</h3>
							<p class="text-xs text-gray-600 mb-2">${event.location}</p>
							<div class="flex gap-1 mb-2">
								${event.tags.map((tag) => `<span class="badge badge-primary badge-xs">${tag}</span>`).join('')}
							</div>
							<button class="btn btn-primary btn-xs" onclick="window.location.href='/event/${event.id}'">
								View Details
							</button>
						</div>
					`;

					marker.bindPopup(popupContent);

					// Add click handler to navigate to event
					marker.on('click', () => {
						goto(`/event/${event.id}`);
					});
				});

				// Add user location marker if provided
				if (userLatitude && userLongitude) {
					const userIcon = new L.Icon({
						iconUrl:
							'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
						shadowUrl:
							'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
						iconSize: [25, 41],
						iconAnchor: [12, 41],
						popupAnchor: [1, -34],
						shadowSize: [41, 41]
					});

					const userMarker = L.marker([userLatitude, userLongitude], { icon: userIcon }).addTo(map);
					userMarker.bindPopup('<b>Your Location</b>');
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

	// Update map when events change
	$effect(() => {
		if (browser && map && events.length > 0) {
			// Clear existing markers
			map.eachLayer((layer: any) => {
				if (layer instanceof (window as any).L.Marker) {
					map.removeLayer(layer);
				}
			});

			// Re-add markers
			events.forEach((event) => {
				const icon =
					event.type === 'Offer'
						? new (window as any).L.Icon({
								iconUrl:
									'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
								shadowUrl:
									'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
								iconSize: [25, 41],
								iconAnchor: [12, 41],
								popupAnchor: [1, -34],
								shadowSize: [41, 41]
							})
						: new (window as any).L.Icon({
								iconUrl:
									'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
								shadowUrl:
									'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
								iconSize: [25, 41],
								iconAnchor: [12, 41],
								popupAnchor: [1, -34],
								shadowSize: [41, 41]
							});

				const marker = (window as any).L.marker([event.latitude, event.longitude], { icon }).addTo(
					map
				);

				const popupContent = `
					<div class="p-2">
						<h3 class="font-bold text-sm">${event.title}</h3>
						<p class="text-xs text-gray-600 mb-2">${event.location}</p>
						<div class="flex gap-1 mb-2">
							${event.tags.map((tag) => `<span class="badge badge-primary badge-xs">${tag}</span>`).join('')}
						</div>
						<button class="btn btn-primary btn-xs" onclick="window.location.href='/event/${event.id}'">
							View Details
						</button>
					</div>
				`;

				marker.bindPopup(popupContent);
				marker.on('click', () => {
					goto(`/event/${event.id}`);
				});
			});

			// Don't fit bounds on update to prevent map "refresh", only on initial load
		}
	});
</script>

<div
	class="h-full min-h-[500px] overflow-hidden rounded-lg border border-base-300"
	bind:this={mapContainer}
></div>

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
