<script lang="ts">
	import { browser } from '$app/environment';

	interface Props {
		latitude: number;
		longitude: number;
		locationName: string;
		userLatitude?: number;
		userLongitude?: number;
		userLocationName?: string;
		height?: string;
	}

	let {
		latitude,
		longitude,
		locationName,
		userLatitude,
		userLongitude,
		userLocationName,
		height = 'h-64'
	}: Props = $props();

	// Haversine distance calculation
	function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
		const R = 6371; // Earth's radius in km
		const dLat = ((lat2 - lat1) * Math.PI) / 180;
		const dLon = ((lon2 - lon1) * Math.PI) / 180;
		const a =
			Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos((lat1 * Math.PI) / 180) *
				Math.cos((lat2 * Math.PI) / 180) *
				Math.sin(dLon / 2) *
				Math.sin(dLon / 2);
		const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	let mapContainer: HTMLElement;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	let map: any = null;
	let isClient = $state(false);

	// Only initialize on client side
	$effect(() => {
		isClient = browser;
		if (isClient && mapContainer && !map) {
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

				// Initialize map
				map = L.map(mapContainer).setView([latitude, longitude], 13);

				// Add OpenStreetMap tiles
				L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
					attribution:
						'© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
					maxZoom: 19
				}).addTo(map);

				// Add event marker
				const eventMarker = L.marker([latitude, longitude]).addTo(map);
				eventMarker
					.bindPopup(
						`<b>📍 Destination: ${locationName}</b><br/>Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}`
					)
					.openPopup();

				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				let userMarker: any = null;

				// Add user marker if user location provided
				if (userLatitude && userLongitude) {
					// Create custom icon for user marker (blue)
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

					userMarker = L.marker([userLatitude, userLongitude], { icon: userIcon }).addTo(map);
					const distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
					userMarker.bindPopup(
						`<b>🏠 You're Here${userLocationName ? ` (${userLocationName})` : ''}</b><br/>Lat: ${userLatitude.toFixed(4)}, Lng: ${userLongitude.toFixed(4)}<br/>Distance: ${distance.toFixed(1)} km`
					);

					// Add distance line
					L.polyline(
						[
							[userLatitude, userLongitude],
							[latitude, longitude]
						],
						{
							color: 'blue',
							weight: 2,
							opacity: 0.7
						}
					).addTo(map);

					// Add circle around user location (500m radius)
					L.circle([userLatitude, userLongitude], {
						color: 'blue',
						fillColor: 'blue',
						fillOpacity: 0.1,
						radius: 500
					}).addTo(map);

					// Add hover tooltips showing clear labels
					eventMarker.on('mouseover', () => {
						eventMarker.bindTooltip(`📍 Destination: ${locationName}`).openTooltip();
					});
					eventMarker.on('mouseout', () => {
						eventMarker.unbindTooltip();
					});

					userMarker.on('mouseover', () => {
						const personName = userLocationName || 'Your Location';
						userMarker.bindTooltip(`🏠 You're Here: ${personName}`).openTooltip();
					});
					userMarker.on('mouseout', () => {
						userMarker.unbindTooltip();
					});
				}

				// Fit map to markers
				const bounds =
					userLatitude && userLongitude
						? [
								[userLatitude, userLongitude],
								[latitude, longitude]
							]
						: [[latitude, longitude]];
				map.fitBounds(bounds, { padding: [20, 20] });
			});
		}

		return () => {
			if (map) {
				map.remove();
				map = null;
			}
		};
	});

	// Update map when coordinates change
	$effect(() => {
		if (isClient && map && latitude && longitude) {
			map.setView([latitude, longitude], 13);

			// Clear existing markers, polylines, and circles
			// eslint-disable-next-line @typescript-eslint/no-explicit-any
			map.eachLayer((layer: any) => {
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				if (
					// eslint-disable-next-line @typescript-eslint/no-explicit-any
					layer instanceof (window as any).L.Marker ||
					// eslint-disable-next-line @typescript-eslint/no-explicit-any
					layer instanceof (window as any).L.Polyline ||
					// eslint-disable-next-line @typescript-eslint/no-explicit-any
					layer instanceof (window as any).L.Circle
				) {
					map.removeLayer(layer);
				}
			});

			// Add event marker
			// eslint-disable-next-line @typescript-eslint/no-explicit-any
			const eventMarker = (window as any).L.marker([latitude, longitude]).addTo(map);
			eventMarker
				.bindPopup(
					`<b>📍 Destination: ${locationName}</b><br/>Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}`
				)
				.openPopup();

			let userMarker: any = null;

			// Add user marker if user location provided
			if (userLatitude && userLongitude) {
				// Create custom icon for user marker (blue)
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				const userIcon = new (window as any).L.Icon({
					iconUrl:
						'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
					shadowUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
					iconSize: [25, 41],
					iconAnchor: [12, 41],
					popupAnchor: [1, -34],
					shadowSize: [41, 41]
				});

				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				userMarker = (window as any).L.marker([userLatitude, userLongitude], {
					icon: userIcon
				}).addTo(map);
				const distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
				userMarker.bindPopup(
					`<b>🏠 You're Here${userLocationName ? ` (${userLocationName})` : ''}</b><br/>Lat: ${userLatitude.toFixed(4)}, Lng: ${userLongitude.toFixed(4)}<br/>Distance: ${distance.toFixed(1)} km`
				);

				// Add distance line
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				(window as any).L.polyline(
					[
						[userLatitude, userLongitude],
						[latitude, longitude]
					],
					{
						color: 'blue',
						weight: 2,
						opacity: 0.7
					}
				).addTo(map);

				// Add circle around user location (500m radius)
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				(window as any).L.circle([userLatitude, userLongitude], {
					color: 'blue',
					fillColor: 'blue',
					fillOpacity: 0.1,
					radius: 500
				}).addTo(map);

				// Add hover tooltips showing clear labels
				eventMarker.on('mouseover', () => {
					eventMarker.bindTooltip(`📍 Destination: ${locationName}`).openTooltip();
				});
				eventMarker.on('mouseout', () => {
					eventMarker.unbindTooltip();
				});

				userMarker.on('mouseover', () => {
					const personName = userLocationName || 'Your Location';
					userMarker.bindTooltip(`🏠 You're Here: ${personName}`).openTooltip();
				});
				userMarker.on('mouseout', () => {
					userMarker.unbindTooltip();
				});
			}

			// Fit map to markers
			const bounds =
				userLatitude && userLongitude
					? [
							[userLatitude, userLongitude],
							[latitude, longitude]
						]
					: [[latitude, longitude]];
			map.fitBounds(bounds, { padding: [20, 20] });
		}
	});
</script>

<div
	class="{height} overflow-hidden rounded-lg border border-base-300"
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
