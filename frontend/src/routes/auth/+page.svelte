<script lang="ts">
	import { goto } from '$app/navigation';
	import { SERVER_URL } from '$lib/config';
	import { auth } from '$lib/stores/auth';
	import { Plus, X } from '@lucide/svelte';

	let activeTab = $state('login');

	// Login form
	let loginUsername = $state('');
	let loginPassword = $state('');
	let loginError = $state('');

	// Register form
	let registerUsername = $state('');
	let registerPassword = $state('');
	let registerEmail = $state('');
	let registerFullName = $state('');
	let registerBiography = $state('');
	let registerInterests = $state<string[]>([]);
	let registerInterestInput = $state('');
	let registerError = $state('');

	async function handleLogin(event: Event) {
		event.preventDefault();
		loginError = '';

		try {
			const response = await fetch(`${SERVER_URL}/auth/login`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ username: loginUsername, password: loginPassword })
			});

			if (!response.ok) {
				const errorData = await response.json().catch(() => ({}));
				throw new Error(errorData.message || `Login failed: ${response.status}`);
			}

			const data = await response.json();
			auth.login(data.token, data.card);
			goto('/');
		} catch (error) {
			console.error('Login error:', error);
			loginError = error instanceof Error ? error.message : 'Login failed. Please try again.';
		}
	}

	async function handleRegister(event: Event) {
		event.preventDefault();
		registerError = '';

		try {
			const response = await fetch(`${SERVER_URL}/auth/register`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					username: registerUsername,
					password: registerPassword,
					email: registerEmail,
					fullName: registerFullName,
					biography: registerBiography,
					interests: registerInterests
				})
			});

			if (!response.ok) {
				throw new Error('Registration failed');
			}

			// After successful registration, switch to login tab
			activeTab = 'login';
			loginUsername = registerUsername;
			loginPassword = registerPassword;
		} catch (error) {
			registerError = 'Registration failed. Please try again.';
		}
	}

	function addInterest() {
		const trimmed = registerInterestInput.trim();
		if (trimmed && !registerInterests.includes(trimmed)) {
			registerInterests = [...registerInterests, trimmed];
			registerInterestInput = '';
		}
	}

	function removeInterest(interest: string) {
		registerInterests = registerInterests.filter((i) => i !== interest);
	}
</script>

<div class="flex min-h-screen items-center justify-center bg-base-200">
	<div class="w-full max-w-md">
		<div class="card bg-base-100 shadow-xl">
			<div class="card-body">
				<h2 class="card-title justify-center text-2xl">Welcome to Hive</h2>

				<div class="tabs-boxed tabs justify-center">
					<button
						class="tab {activeTab === 'login' ? 'tab-active' : ''}"
						onclick={() => (activeTab = 'login')}
					>
						Login
					</button>
					<button
						class="tab {activeTab === 'register' ? 'tab-active' : ''}"
						onclick={() => (activeTab = 'register')}
					>
						Register
					</button>
				</div>

				{#if activeTab === 'login'}
					<form onsubmit={handleLogin}>
						<div class="form-control">
							<label class="label" for="login-username">
								<span class="label-text">Username</span>
							</label>
							<input
								id="login-username"
								type="text"
								placeholder="Enter username"
								class="input-bordered input w-full"
								bind:value={loginUsername}
								required
							/>
						</div>

						<div class="form-control">
							<label class="label" for="login-password">
								<span class="label-text">Password</span>
							</label>
							<input
								id="login-password"
								type="password"
								placeholder="Enter password"
								class="input-bordered input w-full"
								bind:value={loginPassword}
								required
							/>
						</div>

						{#if loginError}
							<div class="mt-4 alert alert-error">
								<span>{loginError}</span>
							</div>
						{/if}

						<div class="form-control mt-6">
							<button type="submit" class="btn btn-primary">Login</button>
						</div>
					</form>
				{:else}
					<form onsubmit={handleRegister}>
						<div class="form-control">
							<label class="label" for="register-username">
								<span class="label-text">Username</span>
							</label>
							<input
								id="register-username"
								type="text"
								placeholder="Enter username"
								class="input-bordered input w-full"
								bind:value={registerUsername}
								required
							/>
						</div>

						<div class="form-control">
							<label class="label" for="register-email">
								<span class="label-text">Email</span>
							</label>
							<input
								id="register-email"
								type="email"
								placeholder="Enter email"
								class="input-bordered input w-full"
								bind:value={registerEmail}
								required
							/>
						</div>

						<div class="form-control">
							<label class="label" for="register-fullname">
								<span class="label-text">Full Name</span>
							</label>
							<input
								id="register-fullname"
								type="text"
								placeholder="Enter full name"
								class="input-bordered input w-full"
								bind:value={registerFullName}
								required
							/>
						</div>

						<div class="form-control">
							<label class="label" for="register-bio">
								<span class="label-text">Biography</span>
							</label>
							<textarea
								id="register-bio"
								placeholder="Tell us about yourself"
								class="textarea-bordered textarea h-24 w-full"
								bind:value={registerBiography}
								required
							></textarea>
						</div>

						<div class="form-control">
							<label class="label" for="register-interests">
								<span class="label-text">Interests</span>
							</label>
							<div class="mb-2 flex gap-2">
								<input
									id="register-interests"
									type="text"
									placeholder="Add an interest (press Enter)"
									class="input-bordered input flex-1"
									bind:value={registerInterestInput}
									onkeydown={(e) => {
										if (e.key === 'Enter') {
											e.preventDefault();
											addInterest();
										}
									}}
								/>
								<button type="button" class="btn btn-outline" onclick={addInterest}>
									<Plus class="h-4 w-4" />
								</button>
							</div>
							{#if registerInterests.length > 0}
								<div class="flex flex-wrap gap-2">
									{#each registerInterests as interest (interest)}
										<div class="badge gap-2 badge-primary">
											{interest}
											<button
												type="button"
												class="btn btn-ghost btn-xs"
												onclick={() => removeInterest(interest)}
											>
												<X class="h-3 w-3" />
											</button>
										</div>
									{/each}
								</div>
							{/if}
						</div>

						<div class="form-control">
							<label class="label" for="register-password">
								<span class="label-text">Password</span>
							</label>
							<input
								id="register-password"
								type="password"
								placeholder="Enter password"
								class="input-bordered input w-full"
								bind:value={registerPassword}
								required
							/>
						</div>

						{#if registerError}
							<div class="mt-4 alert alert-error">
								<span>{registerError}</span>
							</div>
						{/if}

						<div class="form-control mt-6">
							<button type="submit" class="btn btn-primary">Register</button>
						</div>
					</form>
				{/if}
			</div>
		</div>
	</div>
</div>
