<template>
    <div>
        <h2 v-if="authenticated"><span >Active sessions for [<b>{{username}}</b>]</span></h2>

        <div class="alert alert-success" v-if="success" >
            <strong>Session invalidated!</strong>
        </div>
        <div class="alert alert-danger" v-if="error" >
            <strong>An error has occured!</strong> The session could not be invalidated.
        </div>

        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th>IP Address</th>
                    <th>User agent</th>
                    <th>Date</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="session in sessions" :key="session.ipAddress">
                    <td>{{session.ipAddress}}</td>
                    <td>{{session.userAgent}}</td>
                    <td>{{session.tokenDate}}</td>
                    <td>
                        <button type="submit"
                                class="btn btn-primary"
                                v-on:click="invalidate(session.series)">
                            Invalidate
                        </button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script lang="ts" src="./sessions.component.ts">
</script>
